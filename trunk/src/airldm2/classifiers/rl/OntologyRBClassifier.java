package airldm2.classifiers.rl;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openrdf.model.URI;

import airldm2.classifiers.Classifier;
import airldm2.classifiers.rl.estimator.AttributeValue;
import airldm2.classifiers.rl.estimator.ClassEstimator;
import airldm2.classifiers.rl.estimator.OntologyAttributeEstimator;
import airldm2.classifiers.rl.estimator.OntologyBernoulliEstimator;
import airldm2.classifiers.rl.estimator.OntologyMultinomialEstimator;
import airldm2.classifiers.rl.estimator.SingleAttributeEstimator;
import airldm2.classifiers.rl.ontology.Cut;
import airldm2.classifiers.rl.ontology.CutProfile;
import airldm2.classifiers.rl.ontology.GlobalCut;
import airldm2.classifiers.rl.ontology.TBox;
import airldm2.classifiers.rl.ontology.TBoxHelper;
import airldm2.constants.Constants;
import airldm2.core.LDInstance;
import airldm2.core.LDInstances;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.exceptions.RTConfigException;
import airldm2.util.CollectionUtil;
import airldm2.util.MathUtil;
import airldm2.util.Timer;

public class OntologyRBClassifier extends Classifier {

   protected static Logger Log = Logger.getLogger("airldm2.classifiers.rl.OntologyRBClassifier");
   static { Log.setLevel(Level.INFO); }
   
   private static boolean OPTIMIZE_ONTOLOGY = false;
   
   private LDInstances mInstances;
   private RDFDataSource mDataSource;
   private RDFDataDescriptor mDataDesc;
   private TBox mTBox;
   private Map<RbcAttribute,TBoxHelper> mTBoxHelper;
   
   private GlobalCut mGlobalCut;
   
   private int mNumOfClassLabels;
   
   private Map<RbcAttribute,OntologyAttributeEstimator> mAttributeEst;
   
   //[class value]
   private ClassEstimator mClassEst;

   private boolean mUseLeaveCut;
   private Cut mUserCut;

   public OntologyRBClassifier() throws RTConfigException {
      final Properties defaultProps = new Properties();
      try {
         FileInputStream in = new FileInputStream(Constants.CLASSIFIER_PROPERTIES_RESOURCE_PATH);
         defaultProps.load(in);
         in.close();
      } catch (IOException e) {
         throw new RTConfigException("Error reading " + Constants.CLASSIFIER_PROPERTIES_RESOURCE_PATH, e);
      }
      
      String optimizeOntology = defaultProps.getProperty("RBC.optimizeOntology");
      if ("true".equalsIgnoreCase(optimizeOntology)) {
         OPTIMIZE_ONTOLOGY = true;
      }
   }
   
   public void setOptimizeOntology(boolean v) {
      OPTIMIZE_ONTOLOGY = v;
   }
   
   public OntologyRBClassifier(boolean useLeaveCut) throws RTConfigException {
      this();
      mUseLeaveCut = useLeaveCut;
   }
   
   public OntologyRBClassifier(Cut cut) throws RTConfigException {
      this();
      mUserCut = cut;
   }

   @Override
   public void buildClassifier(LDInstances instances) throws Exception {
      Timer.INSTANCE.start("OntoRBC learning");
      
      mInstances = instances;
      mDataDesc = (RDFDataDescriptor) instances.getDesc();
      mDataSource = (RDFDataSource) instances.getDataSource();

      mNumOfClassLabels = mDataDesc.getTargetAttribute().getDomainSize();
      List<RbcAttribute> nonTargetAttributes = mDataDesc.getNonTargetAttributeList();
      int numAttributes = nonTargetAttributes.size();

      mClassEst = new ClassEstimator();
      mClassEst.estimateParameters(mDataSource, mDataDesc);
      
      Log.info("Retrieving TBox... ");
      
      mTBox = mDataSource.getTBox();
      mGlobalCut = new GlobalCut(mTBox, nonTargetAttributes);
      
      Log.info("Initializing estimators... ");
      
      //Initialize
      mAttributeEst = CollectionUtil.makeMap();
      for (int i = 0; i < numAttributes; i++) {
         RbcAttribute att = nonTargetAttributes.get(i);
         OntologyAttributeEstimator est = null;
         if (att.getHierarchyRoot() == null) {
            est = new SingleAttributeEstimator(mTBox, att);
         } else if (att.isHierarchicalHistogram()) {
            est = new OntologyMultinomialEstimator(mTBox, att);
         } else if (att.isHierarchicalSet()) {
            est = new OntologyBernoulliEstimator(mTBox, att);
         }
         
         est.setCut(mGlobalCut.getCut(att));
         mAttributeEst.put(att, est);
         
         est.setDataSource(mDataSource, mDataDesc, mClassEst);
         if (OPTIMIZE_ONTOLOGY) {
            est.estimateAllParameters();
         } else {
            est.estimateParameters();
         }
      }
      
      if (mUseLeaveCut) {
         Log.info("Using leaf cuts... ");
         
         mGlobalCut.resetLeafCuts();
         Log.fine("hierarchy size " + mGlobalCut.size());
         for (RbcAttribute att : nonTargetAttributes) {
            Cut attCut = mGlobalCut.getCut(att);
            if (attCut == null) continue;
            OntologyAttributeEstimator est = mAttributeEst.get(att);
            est.setCut(attCut);
            est.estimateParameters();
         }
         
      } else if (mUserCut != null) {
         Log.info("Using user cut... ");
         
         for (RbcAttribute att : nonTargetAttributes) {
            if (mGlobalCut.getCut(att) == null) continue;
            
            mGlobalCut.replace(att, mUserCut);
            OntologyAttributeEstimator est = mAttributeEst.get(att);
            est.setCut(mUserCut);
            if (!OPTIMIZE_ONTOLOGY) {
               est.estimateParameters();
            }
         }
         
         Log.fine("hierarchy size " + mGlobalCut.size());
         
      } else {
         logParameters(mGlobalCut);
         Log.info("Searching... ");
         
         initTBoxHelper();
         for (RbcAttribute att : nonTargetAttributes) {
            TBoxHelper tBoxHelper = mTBoxHelper.get(att);
            CutProfile profile = tBoxHelper.getMICutProfile();
            double[][] profileInterpolated = profile.interpolate();
            double[][] profileScaled = MathUtil.scale(profileInterpolated);
            int index = MathUtil.findSlope(profileScaled, 1);
            
            Cut cut = profile.getCutWithSize((int)profileInterpolated[index][0]);
            
            OntologyAttributeEstimator est = mAttributeEst.get(att);
            est.setCut(cut);
            if (!OPTIMIZE_ONTOLOGY) {
               est.estimateParameters();
            }
            
            mGlobalCut.replace(att, cut);
         }
         
         Log.info("Search stopped with hierarchy size " + mGlobalCut.size());
      }
      
      Timer.INSTANCE.stop("OntoRBC learning");
   }
   
   private void initTBoxHelper() throws RDFDatabaseException {
      mTBoxHelper = CollectionUtil.makeMap();
      
      List<RbcAttribute> nonTargetAttributes = mDataDesc.getNonTargetAttributeList();
      for (RbcAttribute att : nonTargetAttributes) {
         URI hierarchyRoot = att.getHierarchyRoot();
         if (hierarchyRoot == null) continue;
         
         OntologyMultinomialEstimator est = new OntologyMultinomialEstimator(mTBox, att);
         est.setCut(mGlobalCut.getCut(att));
         est.setDataSource(mDataSource, mDataDesc, mClassEst);
         est.estimateLeafParameters();
         
         List<Map<URI,Double>> valueHistograms = est.getValueHistograms();
         TBoxHelper helper = TBoxHelper.create(mTBox, hierarchyRoot, valueHistograms);
         mTBoxHelper.put(att, helper);
      }
   }

   public GlobalCut getGlobalCut() {
      return mGlobalCut;
   }
   
   public Map<RbcAttribute, OntologyAttributeEstimator> getEstimators() {
      return mAttributeEst;
   }
   
   private void logParameters(GlobalCut globalCut) {
      Log.fine("Global Cut: " + globalCut.toString());
      Log.fine("Class estimator: " + mClassEst.toString());
      Log.fine("Estimators: " + mAttributeEst.values().toString());
   }

   private double computeSizePenalty(AggregatedInstances aggregatedInstances) {
      double parameterSize = mClassEst.paramSize();
      for (OntologyAttributeEstimator est : mAttributeEst.values()) {
         parameterSize += est.paramSize();
      }
      return parameterSize * Math.log(aggregatedInstances.getInstances().size()) / 2.0;
   }

   @Override
   public double classifyInstance(LDInstance instance) throws Exception {
      return 0;
   }

   @Override
   public double[] distributionForInstance(LDInstance instance)
         throws Exception {
      return null;
   }

   public double classifyInstance(AggregatedInstance instance) {
      double[] dist = distributionForInstance(instance, 0);
      return MathUtil.maxIndex(dist);
   }
   
   public double[] classConditionalsForInstance(AggregatedInstance instance) {
      Map<RbcAttribute,AttributeValue> values = instance.getAttributeValues();
      double[] logDist = new double[mNumOfClassLabels];
      Arrays.fill(logDist, 0.0);
      
      for (Entry<RbcAttribute, OntologyAttributeEstimator> entry : mAttributeEst.entrySet()) {
         RbcAttribute att = entry.getKey();
         OntologyAttributeEstimator estimator = entry.getValue();
         AttributeValue attValue = values.get(att);
         if (!estimator.isValid()) continue;
         
         double[] attLL = new double[mNumOfClassLabels];
         for (int c = 0; c < logDist.length; c++) {
            attLL[c] = estimator.computeLikelihood(c, attValue);
            logDist[c] += attLL[c];
         }
      }
         
      for (int c = 0; c < logDist.length; c++) {
         logDist[c] += mClassEst.computeLikelihood(c);
      }
      Log.fine(Arrays.toString(logDist));
      return logDist;
   }
   
   public double[] distributionForInstance(AggregatedInstance instance, int actual) {
      double[] logDist = classConditionalsForInstance(instance);
      MathUtil.normalizeLog(logDist);
      return logDist;
   }

   public Map<RbcAttribute,OntologyAttributeEstimator> getCountsForTest() {
      return mAttributeEst;
   }
   
   public ClassEstimator getClassCountsForTest() {
      return mClassEst;
   }
   
}
