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

import weka.classifiers.evaluation.ConfusionMatrix;
import weka.classifiers.evaluation.NominalPrediction;

import airldm2.classifiers.Classifier;
import airldm2.classifiers.rl.estimator.AttributeValue;
import airldm2.classifiers.rl.estimator.ClassEstimator;
import airldm2.classifiers.rl.estimator.OntologyAttributeEstimator;
import airldm2.classifiers.rl.estimator.OntologyBernoulliEstimator;
import airldm2.classifiers.rl.estimator.OntologyMultinomialEstimator;
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
import airldm2.exceptions.RDFDatabaseException;
import airldm2.exceptions.RTConfigException;
import airldm2.util.CollectionUtil;
import airldm2.util.MathUtil;
import airldm2.util.Timer;

public class OntologyRBClassifier extends Classifier {

   protected static Logger Log = Logger.getLogger("airldm2.classifiers.rl.OntologyRBClassifier");
   static { Log.setLevel(Level.WARNING); }
   
   private boolean OPTIMIZE_ONTOLOGY = false;
   
   private LDInstances mInstances;
   private RDFDataSource mDataSource;
   private RDFDataDescriptor mDataDesc;
   private TBox mTBox;
   
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
      
      Log.warning("Retrieving TBox... ");
      
      mTBox = mDataSource.getTBox();
      mGlobalCut = new GlobalCut(mTBox, nonTargetAttributes);
      
      Log.warning("Initializing estimators... ");
      
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
         Log.warning("Using leaf cuts... ");
         
         mGlobalCut.resetLeafCuts();
         Log.warning("hierarchy size " + mGlobalCut.size());
         for (RbcAttribute att : nonTargetAttributes) {
            Cut attCut = mGlobalCut.getCut(att);
            if (attCut == null) continue;
            OntologyAttributeEstimator est = mAttributeEst.get(att);
            est.setCut(attCut);
            est.estimateParameters();
         }
         
      } else if (mUserCut != null) {
         Log.warning("Using user cut... ");
         
         for (RbcAttribute att : nonTargetAttributes) {
            if (mGlobalCut.getCut(att) == null) continue;
            
            mGlobalCut.replace(att, mUserCut);
            OntologyAttributeEstimator est = mAttributeEst.get(att);
            est.setCut(mUserCut);
            est.estimateParameters();
         }
         
         Log.warning("hierarchy size " + mGlobalCut.size());
         
      } else {
         logParameters(mGlobalCut);
         Log.warning("Searching... ");
         
         //Greedy search global cut
         double bestScore = computeModelScore();
         while (true) {
            double newBestScore = Double.NEGATIVE_INFINITY;
            GlobalCut newBestGlobalCut = null;
            
            for (RbcAttribute att : nonTargetAttributes) {
               Cut attCut = mGlobalCut.getCut(att);
               if (attCut == null) continue;
               OntologyAttributeEstimator est = mAttributeEst.get(att);
               
               for (Cut attRefinement : attCut.refine()) {
                  est.setCut(attRefinement);
                  if (!OPTIMIZE_ONTOLOGY) {
                     est.estimateParameters();
                  }
                  
                  double score = computeModelScore();
                  if (score > newBestScore) {
                     GlobalCut globalCut = mGlobalCut.copy();
                     globalCut.replace(att, attRefinement);
                     
                     //Log.warning("New score " + score + " " + globalCut.toString());
                     newBestScore = score;
                     newBestGlobalCut = globalCut;
                  }
               }
               est.setCut(attCut);
            }         
            
            if (newBestGlobalCut.size() < 50 || newBestScore > bestScore) {
               bestScore = newBestScore;
               mGlobalCut = newBestGlobalCut;
               
               Log.warning("New best global cut found with score " + newBestScore + " and hierarchy size " + mGlobalCut.size());
               //logParameters(mGlobalCut);
               
               updateEstimatorCuts(mGlobalCut);
            } else {
               break;
            }
         }
         
         logParameters(mGlobalCut);
         Log.warning(mAttributeEst.toString());
      }
      
      Timer.INSTANCE.stop("OntoRBC learning");
   }

   public GlobalCut getGlobalCut() {
      return mGlobalCut;
   }
   
   public Map<RbcAttribute, OntologyAttributeEstimator> getEstimators() {
      return mAttributeEst;
   }
   
   private void logParameters(GlobalCut globalCut) {
      Log.warning("Global Cut: " + globalCut.toString());
      Log.warning("Class estimator: " + mClassEst.toString());
      Log.warning("Estimators: " + mAttributeEst.values().toString());
   }

   private void updateEstimatorCuts(GlobalCut globalCut) {
      for (Entry<RbcAttribute, OntologyAttributeEstimator> entry : mAttributeEst.entrySet()) {
         RbcAttribute key = entry.getKey();
         OntologyAttributeEstimator est = entry.getValue();
         Cut cut = mGlobalCut.getCut(key);
         est.setCut(cut);
      }
   }

   private double computeModelScore() throws RDFDatabaseException {
      AggregatedInstances aggregatedInstances = InstanceAggregator.aggregateSample(mInstances, 0.1);
      
      //double fit = computeTrainingAccuracy(aggregatedInstances);
      double fit = computeCLL(aggregatedInstances);
      double sizePenalty = computeSizePenalty(aggregatedInstances);
      Log.info("fit=" + fit + " sizePenalty=" + sizePenalty + " model=" + (fit - sizePenalty));
      return fit;// - sizePenalty;
   }

   private double computeTrainingAccuracy(AggregatedInstances aggregatedInstances) throws RDFDatabaseException {
      Log.info("Retrieving sample instances... ");
      
      String[] classLabels = mDataDesc.getClassLabels();
      ConfusionMatrix wekaConfusionMatrix = new ConfusionMatrix(classLabels);
      
      for (AggregatedInstance i : aggregatedInstances.getInstances()) {
         double actual = i.getLabel();
         double[] distribution = distributionForInstance(i, (int)actual);
         try {
            wekaConfusionMatrix.addPrediction(new NominalPrediction(actual, distribution));
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
      
      return (1.0 - wekaConfusionMatrix.errorRate()) * mClassEst.getNumInstances();
   }
   
   private double computeCLL(AggregatedInstances aggregatedInstances) throws RDFDatabaseException {
      Log.info("Retrieving sample instances... ");
      
      double result = 0.0;
      for (AggregatedInstance i : aggregatedInstances.getInstances()) {
         double cll = cllForInstance(i, (int)i.getLabel());
         result += cll;
      }
      
      return result * aggregatedInstances.getInstances().size();
   }

   private double computeSizePenalty(AggregatedInstances aggregatedInstances) {
      double parameterSize = mClassEst.paramSize();
      for (OntologyAttributeEstimator est : mAttributeEst.values()) {
         parameterSize += est.paramSize();
      }
      return parameterSize * Math.log(aggregatedInstances.getInstances().size()) / 2.0;
      //return parameterSize * Math.log(mClassEst.getNumInstances()) / 2.0;
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
      Log.info(Arrays.toString(logDist));
      return logDist;
   }
   
   public double[] distributionForInstance(AggregatedInstance instance, int actual) {
      double[] logDist = classConditionalsForInstance(instance);
      MathUtil.normalizeLog(logDist);
      return logDist;
   }

   private double cllForInstance(AggregatedInstance instance, int actual) {
      double[] dist = classConditionalsForInstance(instance);
      double sum = MathUtil.sumLog(dist);
      return dist[actual] - sum;
   }


   public Map<RbcAttribute,OntologyAttributeEstimator> getCountsForTest() {
      return mAttributeEst;
   }
   
   public ClassEstimator getClassCountsForTest() {
      return mClassEst;
   }
   
}
