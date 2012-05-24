package airldm2.classifiers.rl;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import airldm2.classifiers.Classifier;
import airldm2.classifiers.rl.estimator.AttributeEstimator;
import airldm2.classifiers.rl.estimator.AttributeValue;
import airldm2.classifiers.rl.estimator.ClassEstimator;
import airldm2.classifiers.rl.estimator.OntologyAttributeEstimator;
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
import airldm2.util.ArrayUtil;
import airldm2.util.CollectionUtil;
import airldm2.util.MathUtil;

public class OntologyRBClassifier extends Classifier {

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
      
      mTBox = mDataSource.getTBox();
      mGlobalCut = new GlobalCut(mTBox, nonTargetAttributes);
      
      //Initialize
      mAttributeEst = CollectionUtil.makeMap();
      for (int i = 0; i < numAttributes; i++) {
         RbcAttribute att = nonTargetAttributes.get(i);
         OntologyAttributeEstimator est = null;
         if (att.getHierarchyRoot() == null) {
            est = new SingleAttributeEstimator(att);
         } else if (att.isHierarchicalHistogram()) {
            est = new OntologyMultinomialEstimator(att);
         } else {
            est = new SetAttributeEstimator(att);
         }
         est.setCut(mGlobalCut.getCut(att));
         mAttributeEst.put(att, est);
         
         if (OPTIMIZE_ONTOLOGY) {
            est.estimateAllParameters(mDataSource, mDataDesc, mClassEst, mTBox);
         } else {
            est.estimateParameters(mDataSource, mDataDesc, mClassEst);
         }
      }
      
      System.out.println("Global Cut:");
      System.out.println(mGlobalCut);
      System.out.println("Estimators:");
      System.out.println(mAttributeEst.values());
      
      //Greedy search global cut
      double bestScore = Double.MIN_VALUE;
      while (true) {
         double newBestScore = Double.MIN_VALUE;
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
               double score = computeCMDL();
               if (score > newBestScore) {
                  newBestScore = score;
                  newBestGlobalCut = globalCut;
               }
            }
            est.setCut(attCut);
         }         
         
         if (newBestScore > bestScore) {
            bestScore = newBestScore;
            mGlobalCut = newBestGlobalCut;
            
            System.out.println("Global Cut:");
            System.out.println(mGlobalCut);
            
            System.out.println("Estimators:");
            System.out.println(mAttributeEst.values());
            updateEstimatorCuts(mGlobalCut);
         } else {
            break;
         }
      }
   }
   
   private void updateEstimatorCuts(GlobalCut globalCut) {
      for (Entry<RbcAttribute, OntologyAttributeEstimator> entry : mAttributeEst.entrySet()) {
         RbcAttribute key = entry.getKey();
         OntologyAttributeEstimator est = entry.getValue();
         Cut cut = mGlobalCut.getCut(key);
         est.setCut(cut);
      }
   }

   private double computeCMDL() {
      //return computeLL() - computeSizePenalty();
      double CLL = computeCLL();
      double sizePenalty = computeSizePenalty();
      System.out.println("CLL=" + CLL + " sizePenalty=" + sizePenalty);
      return CLL - sizePenalty;
   }

   private double computeSizePenalty() {
      return mGlobalCut.size() * mNumOfClassLabels * MathUtil.lg(mClassEst.getNumInstances()) / 2.0;
   }

   private double computeLL() {
      double result = mClassEst.computeLL();
      for (OntologyAttributeEstimator est : mAttributeEst.values()) {
         result += est.computeLL();
      }
      return result;
   }
   
   private double computeDualLL() {
      double result = mClassEst.computeDualLL();
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
      System.out.println("LL=" + LL + " DualLL=" + DualLL);
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
      double[] dist = distributionForInstance(instance);
      return ArrayUtil.maxIndex(dist);
   }
   
   public double[] distributionForInstance(AggregatedInstance instance) {
      List<AttributeValue> values = instance.getAttributeValues();
      double[] dist = new double[mNumOfClassLabels];
      Arrays.fill(dist, 1.0);
      
      for (int c = 0; c < dist.length; c++) {
         for (int a = 0; a < mAttributeEst.size(); a++) {
            AttributeEstimator estimator = mAttributeEst.get(a);
            AttributeValue attValue = values.get(a);
            double attLikelihood = estimator.computeLikelihood(c, attValue);
            dist[c] *= attLikelihood;
         }
         
         dist[c] *= mClassEst.computeLikelihood(c);
      }
      
      ArrayUtil.normalize(dist);
      
      return dist;
   }

   public Map<RbcAttribute,OntologyAttributeEstimator> getCountsForTest() {
      return mAttributeEst;
   }
   
   public ClassEstimator getClassCountsForTest() {
      return mClassEst;
   }
   
}
