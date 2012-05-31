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

import airldm2.classifiers.Classifier;
import airldm2.classifiers.rl.estimator.AttributeValue;
import airldm2.classifiers.rl.estimator.ClassEstimator;
import airldm2.classifiers.rl.estimator.OntologyAttributeEstimator;
import airldm2.classifiers.rl.estimator.OntologyGaussianFixedVarianceEstimator;
import airldm2.classifiers.rl.estimator.OntologyMultinomialEstimator;
import airldm2.classifiers.rl.estimator.SetAttributeEstimator;
import airldm2.classifiers.rl.estimator.SingleAttributeEstimator;
import airldm2.classifiers.rl.ontology.Cut;
import airldm2.classifiers.rl.ontology.GlobalCut;
import airldm2.classifiers.rl.ontology.TBox;
import airldm2.constants.Constants;
import airldm2.core.LDInstance;
import airldm2.core.LDInstances;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.exceptions.RTConfigException;
import airldm2.util.MathUtil;
import airldm2.util.CollectionUtil;

public class OntologyRBClassifier extends Classifier {

   protected static Logger Log = Logger.getLogger("airldm2.classifiers.rl.OntologyRBClassifier");
   static { Log.setLevel(Level.INFO); }
   
   private boolean OPTIMIZE_ONTOLOGY = false;
   
   private RDFDataSource mDataSource;
   private RDFDataDescriptor mDataDesc;
   private TBox mTBox;
   
   private GlobalCut mGlobalCut;
   
   private int mNumOfClassLabels;
   
   private Map<RbcAttribute,OntologyAttributeEstimator> mAttributeEst;
   
   //[class value]
   private ClassEstimator mClassEst;
      
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
   
   @Override
   public void buildClassifier(LDInstances instances) throws Exception {
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
         } else if (att.isCutSum()) {
            est = new OntologyGaussianFixedVarianceEstimator(mTBox, att);
         } else {
            est = new SetAttributeEstimator(mTBox, att);
         }
         est.setCut(mGlobalCut.getCut(att));
         mAttributeEst.put(att, est);
         
         if (OPTIMIZE_ONTOLOGY) {
            est.estimateAllParameters(mDataSource, mDataDesc, mClassEst);
         } else {
            est.estimateParameters(mDataSource, mDataDesc, mClassEst);
         }
      }
      
      logParameters(mGlobalCut);
      Log.info("Searching... ");
      
      //Greedy search global cut
      double bestScore = computeCMDL(mGlobalCut);
      while (true) {
         double newBestScore = Double.NEGATIVE_INFINITY;
         GlobalCut newBestGlobalCut = null;
         
         for (RbcAttribute att : nonTargetAttributes) {
            Cut attCut = mGlobalCut.getCut(att);
            if (attCut == null) continue;
            OntologyAttributeEstimator est = mAttributeEst.get(att);
            
            for (Cut attRefinement : attCut.refine()) {
               GlobalCut globalCut = mGlobalCut.copy();
               globalCut.replace(att, attRefinement);
               est.setCut(attRefinement);
               est.estimateParameters(mDataSource, mDataDesc, mClassEst);
               
               Log.info("Trying new global cut: " + globalCut.toString());
               logParameters(globalCut);
               
               double score = computeCMDL(globalCut);
               
               if (score > newBestScore) {
                  Log.info("New score " + score + " " + globalCut.toString());
                  newBestScore = score;
                  newBestGlobalCut = globalCut;
               }
            }
            est.setCut(attCut);
         }         
         
         if (newBestScore > bestScore) {
            bestScore = newBestScore;
            mGlobalCut = newBestGlobalCut;
            
            Log.info("New best global cut found with score " + newBestScore);
            logParameters(mGlobalCut);
            
            updateEstimatorCuts(mGlobalCut);
         } else {
            break;
         }
      }
      
      Log.info(mAttributeEst.toString());
   }
   
   public GlobalCut getGlobalCut() {
      return mGlobalCut;
   }
   
   public Map<RbcAttribute, OntologyAttributeEstimator> getEstimators() {
      return mAttributeEst;
   }
   
   private void logParameters(GlobalCut globalCut) {
      Log.info("Global Cut: " + globalCut.toString());
      Log.info("Class estimator: " + mClassEst.toString());
      Log.info("Estimators: " + mAttributeEst.values().toString());
   }

   private void updateEstimatorCuts(GlobalCut globalCut) {
      for (Entry<RbcAttribute, OntologyAttributeEstimator> entry : mAttributeEst.entrySet()) {
         RbcAttribute key = entry.getKey();
         OntologyAttributeEstimator est = entry.getValue();
         Cut cut = mGlobalCut.getCut(key);
         est.setCut(cut);
      }
   }

   private double computeCMDL(GlobalCut globalCut) {
      double CLL = computeCLL();
      double sizePenalty = computeSizePenalty(globalCut);
      Log.info("CLL=" + CLL + " sizePenalty=" + sizePenalty);
      return CLL - sizePenalty;
   }

   private double computeSizePenalty(GlobalCut globalCut) {
      double parameterSize = 0.0;
      for (OntologyAttributeEstimator est : mAttributeEst.values()) {
         parameterSize += est.paramSize();
      }
      return parameterSize * Math.log(mClassEst.getNumInstances()) / 2.0;
   }

   private double computeLL() {
      double result = mClassEst.computeLL();
      Log.info("Class LL=" + result);
      for (OntologyAttributeEstimator est : mAttributeEst.values()) {
         result += est.computeLL();
      }
      return result;
   }
   
   private double computeDualLL() {
      double result = mClassEst.computeDualLL();
      Log.info("Class DualLL=" + result);
      for (OntologyAttributeEstimator est : mAttributeEst.values()) {
         result += est.computeDualLL();
      }
      return result;
   }

   private double computeCLL() {
      final double PI2 = Math.PI * Math.PI;
      final double ALPHA = (PI2 + 6) / 24;
      final double BETA = (PI2 - 18) / 24;
      double LL = computeLL();
      double DualLL = computeDualLL();
      Log.info("LL=" + LL + " DualLL=" + DualLL);
      return ALPHA * LL + BETA * DualLL;
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
   
   public Map<RbcAttribute, Double> LogDiffSum = CollectionUtil.makeMap(); 
   
   public double[] distributionForInstance(AggregatedInstance instance, int actual) {
      Map<RbcAttribute,AttributeValue> values = instance.getAttributeValues();
      double[] dist = new double[mNumOfClassLabels];
      Arrays.fill(dist, 0.0);
      
      for (Entry<RbcAttribute, OntologyAttributeEstimator> entry : mAttributeEst.entrySet()) {
         RbcAttribute att = entry.getKey();
         OntologyAttributeEstimator estimator = entry.getValue();
         AttributeValue attValue = values.get(att);
         if (!estimator.isValid()) continue;
         
         double[] attLL = new double[mNumOfClassLabels];
         for (int c = 0; c < dist.length; c++) {
            attLL[c] = estimator.computeLikelihood(c, attValue);
            dist[c] += attLL[c];
         }
         
         Double s = LogDiffSum.get(att);
         if (s == null) s = 0.0;
         s += attLL[actual] - attLL[1-actual];
         LogDiffSum.put(att, s);
         //System.out.print(att.getName() + " " + (attLL[actual] - attLL[1-actual]) + " ");
      }
         
      for (int c = 0; c < dist.length; c++) {
         dist[c] += mClassEst.computeLikelihood(c);
      }
      
      MathUtil.normalizeLog(dist);
      return dist;
   }

   public Map<RbcAttribute,OntologyAttributeEstimator> getCountsForTest() {
      return mAttributeEst;
   }
   
   public ClassEstimator getClassCountsForTest() {
      return mClassEst;
   }
   
}
