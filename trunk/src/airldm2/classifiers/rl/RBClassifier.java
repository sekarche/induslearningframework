package airldm2.classifiers.rl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import weka.classifiers.evaluation.ConfusionMatrix;
import airldm2.classifiers.Classifier;
import airldm2.classifiers.Evaluation;
import airldm2.classifiers.rl.estimator.AttributeEstimator;
import airldm2.classifiers.rl.estimator.AttributeValue;
import airldm2.classifiers.rl.estimator.ClassEstimator;
import airldm2.core.LDInstance;
import airldm2.core.LDInstances;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.util.CollectionUtil;
import airldm2.util.MathUtil;

public class RBClassifier extends Classifier {

   private RDFDataSource mDataSource;
   private RDFDataDescriptor mDataDesc;

   private int mNumOfClassLabels;
   private Map<RbcAttribute,AttributeEstimator> mAttributeEst;
   private ClassEstimator mClassEst;
      
   @Override
   public void buildClassifier(LDInstances instances) throws Exception {
      mDataDesc = (RDFDataDescriptor) instances.getDesc();
      mDataSource = (RDFDataSource) instances.getDataSource();

      mNumOfClassLabels = mDataDesc.getTargetAttribute().getDomainSize();
      List<RbcAttribute> nonTargetAttributes = mDataDesc.getNonTargetAttributeList();
      int numAttributes = nonTargetAttributes.size();

      mAttributeEst = CollectionUtil.makeMap();
      mClassEst = new ClassEstimator();
      mClassEst.estimateParameters(mDataSource, mDataDesc);
      
      for (int i = 0; i < numAttributes; i++) {
         RbcAttribute att = nonTargetAttributes.get(i);
         AttributeEstimator estimator = att.getEstimator();
         estimator.setDataSource(mDataSource, mDataDesc, mClassEst);
         estimator.estimateParameters();
         mAttributeEst.put(att, estimator);
      }
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
      return MathUtil.maxIndex(dist);
   }
   
   public double[] distributionForInstance(AggregatedInstance instance) {
      Map<RbcAttribute,AttributeValue> values = instance.getAttributeValues();
      double[] dist = new double[mNumOfClassLabels];
      Arrays.fill(dist, 0.0);
      
      for (int c = 0; c < dist.length; c++) {
         for (Entry<RbcAttribute, AttributeEstimator> entry : mAttributeEst.entrySet()) {
            RbcAttribute att = entry.getKey();
            AttributeEstimator estimator = entry.getValue();
            AttributeValue attValue = values.get(att);
            double attLikelihood = estimator.computeLikelihood(c, attValue);
            dist[c] += attLikelihood;
         }
         
         dist[c] += mClassEst.computeLikelihood(c);
      }
      
      MathUtil.normalizeLog(dist);
      
      return dist;
   }

   public Map<RbcAttribute,AttributeEstimator> getCountsForTest() {
      return mAttributeEst;
   }
   
   public ClassEstimator getClassCountsForTest() {
      return mClassEst;
   }
   
   public static void main(String[] args) {
      try {
         RBClassifier rbc = new RBClassifier();
         ConfusionMatrix matrix = Evaluation.evaluateRBCModel(rbc, args);
         System.out.println(matrix.toString("===Confusion Matrix==="));
      } catch (Exception e) {
         System.out.println(e.getMessage());
         e.printStackTrace();
      }
   }
   
}
