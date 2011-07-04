package airldm2.classifiers.rl;

import java.util.Arrays;
import java.util.List;

import weka.classifiers.evaluation.ConfusionMatrix;
import airldm2.classifiers.Classifier;
import airldm2.classifiers.Evaluation;
import airldm2.core.ISufficentStatistic;
import airldm2.core.LDInstance;
import airldm2.core.LDInstances;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.database.rdf.SuffStatQueryParameter;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.util.ArrayUtil;
import airldm2.util.CollectionUtil;

public class RelationalBayesianClassifier extends Classifier {

   private RDFDataSource mDataSource;
   private RDFDataDescriptor mDataDesc;
   private int mNumInstances;

   //[attribute name][class value][attribute value]
   private List<ClassValueCount> mCounts;
   
   //[attribute name][class value]
   private List<ClassCount> mAttributeClassCounts;
   
   //[class value]
   private ClassCount mClassCounts;
      
   @Override
   public void buildClassifier(LDInstances instances) throws Exception {
      mDataDesc = (RDFDataDescriptor) instances.getDesc();
      mDataSource = (RDFDataSource) instances.getDataSource();
      
      List<RbcAttribute> nonTargetAttributes = mDataDesc.getNonTargetAttributeList();
      RbcAttribute targetAttribute = mDataDesc.getTargetAttribute();

      int numOfClassLabels = targetAttribute.getDomainSize();      
      int numAttributes = nonTargetAttributes.size();

      // [attribute name][class label][attribute value]
      mCounts = CollectionUtil.makeList();
      // [attribute name][class value]
      mAttributeClassCounts = CollectionUtil.makeList();
      
      for (int i = 0; i < numAttributes; i++) {
         RbcAttribute currAttribute = nonTargetAttributes.get(i);
         ClassValueCount counts = getCounts(currAttribute);
         addAttributeCounts(counts);
      }
      
      //Explicitly ask for class count since every attribute may be INDEPENDENT_VAL
      double[] classCounts = new double[numOfClassLabels];
      for (int j = 0; j < numOfClassLabels; j++) {
         ISufficentStatistic tempSuffStat = mDataSource.getSufficientStatistic(new SuffStatQueryParameter(mDataDesc.getTargetType(), targetAttribute, j));
         classCounts[j] = tempSuffStat.getValue().intValue();
      }
      mClassCounts = new ClassCount(classCounts);
      
      for (int i = 0; i < numOfClassLabels; i++) {
         mNumInstances += (int) classCounts[i];
      }
   }

   //double[class value][attribute value]
   public ClassValueCount getCounts(RbcAttribute att) throws RDFDatabaseException {
      RbcAttribute targetAttribute = mDataDesc.getTargetAttribute();
      int numOfClassLabels = targetAttribute.getDomainSize();
      int numOfAttributeValues = att.getDomainSize();

      double[][] counts = new double[numOfClassLabels][numOfAttributeValues];
      
      for (int j = 0; j < numOfClassLabels; j++) {
         for (int k = 0; k < numOfAttributeValues; k++) {
            SuffStatQueryParameter queryParam = new SuffStatQueryParameter(mDataDesc.getTargetType(), targetAttribute, j, att, k);
            ISufficentStatistic tempSuffStat = mDataSource.getSufficientStatistic(queryParam);
            counts[j][k] = tempSuffStat.getValue().intValue();
            
            //System.out.println(queryParam);
            //System.out.println(tempSuffStat.getValue());
         }
      }
      
      return new ClassValueCount(counts);
   }
   
   public void addAttributeCounts(ClassValueCount counts) {
      RbcAttribute targetAttribute = mDataDesc.getTargetAttribute();
      int numOfClassLabels = targetAttribute.getDomainSize();
      
      mCounts.add(counts);
      
      //Cache mAttributeClassCounts for optimization (for classification)
      double[] attCounts = new double[numOfClassLabels];
      for (int j = 0; j < numOfClassLabels; j++) {
         for (int k = 0; k < counts.size(j); k++) {
            attCounts[j] += counts.get(j, k);
         }
      }
      mAttributeClassCounts.add(new ClassCount(attCounts));
   }

   public void removeLastAttributeCounts() {
      mCounts.remove(mCounts.size() - 1);
      mAttributeClassCounts.remove(mAttributeClassCounts.size() - 1);
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
      List<ValueIndexCount> featureValueIndexCount = instance.getAttributeValueIndexCount();
      double[] dist = new double[mClassCounts.size()];
      Arrays.fill(dist, 1.0);
      
      for (int c = 0; c < dist.length; c++) {
         for (int a = 0; a < featureValueIndexCount.size(); a++) {
            ValueIndexCount valueIndexCount = featureValueIndexCount.get(a);
            for (int v = 0; v < valueIndexCount.size(); v++) {
               if (valueIndexCount.get(v) == 0) continue;
               try{
               ClassValueCount count = mCounts.get(a);
               ClassCount attCount = mAttributeClassCounts.get(a);
               //With Laplace correction
               double pVpC = (count.get(c, v) + 1) / (attCount.get(c) + count.size(c));
               
               if (valueIndexCount.get(v) == 1) {
                  dist[c] *= pVpC;
               } else {
                  dist[c] *= Math.pow(pVpC, valueIndexCount.get(v));
               } 
               }catch (ArrayIndexOutOfBoundsException ex) {
                  System.out.println(valueIndexCount);
                  System.out.println(mCounts.get(a));
               }
            }
         }
         
       //With Laplace correction
         dist[c] *= (mClassCounts.get(c) + 1) / (mNumInstances + mClassCounts.size());
      }
      
      ArrayUtil.normalize(dist);
      return dist;
   }

   public List<ClassValueCount> getCountsForTest() {
      return mCounts;
   }
   
   public List<ClassCount> getAttributeClassCountsForTest() {
      return mAttributeClassCounts;
   }

   public ClassCount getClassCountsForTest() {
      return mClassCounts;
   }

   public int getNumInstances() {
      return mNumInstances;
   }
   
   public static void main(String[] args) {
      try {
         RelationalBayesianClassifier rbc = new RelationalBayesianClassifier();
         ConfusionMatrix matrix = Evaluation.evaluateRBCModel(rbc, args);
         System.out.println(matrix.toString("===Confusion Matrix==="));
      } catch (Exception e) {
         System.out.println(e.getMessage());
         e.printStackTrace();
      }
   }
   
}
