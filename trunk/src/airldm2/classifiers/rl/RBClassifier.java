package airldm2.classifiers.rl;

import java.util.Arrays;
import java.util.List;

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
import airldm2.util.ArrayUtil;
import airldm2.util.CollectionUtil;

public class RBClassifier extends Classifier {

   private RDFDataSource mDataSource;
   private RDFDataDescriptor mDataDesc;

   private int mNumOfClassLabels;
   
   //[attribute name][class value][attribute value]
   private List<AttributeEstimator> mAttributeEst;
   
   //[class value]
   private ClassEstimator mClassEst;
      
   @Override
   public void buildClassifier(LDInstances instances) throws Exception {
      mDataDesc = (RDFDataDescriptor) instances.getDesc();
      mDataSource = (RDFDataSource) instances.getDataSource();

      mNumOfClassLabels = mDataDesc.getTargetAttribute().getDomainSize();
      List<RbcAttribute> nonTargetAttributes = mDataDesc.getNonTargetAttributeList();
      int numAttributes = nonTargetAttributes.size();

      // [attribute name][class label][attribute value]
      mAttributeEst = CollectionUtil.makeList();
      
      mClassEst = new ClassEstimator();
      mClassEst.estimateParameters(mDataSource, mDataDesc);
      
      for (int i = 0; i < numAttributes; i++) {
         RbcAttribute att = nonTargetAttributes.get(i);
         AttributeEstimator estimator = att.getEstimator();
         estimator.estimateParameters(mDataSource, mDataDesc, mClassEst);
         mAttributeEst.add(estimator);
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

   public List<AttributeEstimator> getCountsForTest() {
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
