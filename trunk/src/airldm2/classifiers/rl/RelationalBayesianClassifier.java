package airldm2.classifiers.rl;

import java.util.Arrays;
import java.util.List;

import airldm2.classifiers.Classifier;
import airldm2.core.ISufficentStatistic;
import airldm2.core.LDInstance;
import airldm2.core.LDInstances;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.core.rl.ValueType;
import airldm2.database.rdf.SuffStatQueryParameter;
import airldm2.util.ArrayUtil;

public class RelationalBayesianClassifier extends Classifier {

   private RDFDataSource mDataSource;
   private RDFDataDescriptor mDataDesc;
   private int mNumInstances;

   //[attribute name][class value][attribute value]
   private double[][][] mCounts;
   
   //[attribute name][class value]
   private double[][] mAttributeClassCounts;
   
   //[class value]
   private double[] mClassCounts;
   
   @Override
   public void buildClassifier(LDInstances instances) throws Exception {
      mDataDesc = (RDFDataDescriptor) instances.getDesc();
      mDataSource = (RDFDataSource) instances.getDataSource();
      
      List<RbcAttribute> nonTargetAttributes = mDataDesc.getAttributeList();
      RbcAttribute targetAttribute = mDataDesc.getTargetAttribute();


      ValueType targetAttributeType = targetAttribute.getValueType();
      int numOfClassLabels = targetAttributeType.domainSize();
      mClassCounts = new double[numOfClassLabels];

      int numAttributes = nonTargetAttributes.size();

      // [attribute name][class label][attribute value]
      mCounts = new double[numAttributes][numOfClassLabels][];
      // [attribute name][class value]
      mAttributeClassCounts = new double[numAttributes][numOfClassLabels];
      
      // find possible values for each attribute and allocate memory
      for (int i = 0; i < numAttributes; i++) {
         int numValuesCurrAttrib = nonTargetAttributes.get(i).getValueType().domainSize();
         for (int j = 0; j < numOfClassLabels; j++) {
            mCounts[i][j] = new double[numValuesCurrAttrib];
         }
      }

      for (int i = 0; i < numAttributes; i++) {
         RbcAttribute currAttribute = nonTargetAttributes.get(i);
         int numOfAttributeValues = currAttribute.getValueType().domainSize();
         for (int j = 0; j < numOfClassLabels; j++) {
            for (int k = 0; k < numOfAttributeValues; k++) {
               SuffStatQueryParameter queryParam = new SuffStatQueryParameter(mDataDesc.getTargetType(), targetAttribute, j, currAttribute, k);
               ISufficentStatistic tempSuffStat = mDataSource.getSufficientStatistic(queryParam);
               mCounts[i][j][k] = tempSuffStat.getValue().intValue();
               
               System.out.println(queryParam);
               System.out.println(tempSuffStat.getValue());
            }
         }
      }
      
      //Cache mAttributeClassCounts for optimization (for classification)
      for (int i = 0; i < numAttributes; i++) {
         for (int j = 0; j < numOfClassLabels; j++) {
            for (int k = 0; k < mCounts[i][j].length; k++) {
               mAttributeClassCounts[i][j] += mCounts[i][j][k];
            }
         }
      }

      //Explicitly ask for class count since every attribute may be INDEPENDENT_VAL
      for (int j = 0; j < numOfClassLabels; j++) {
         ISufficentStatistic tempSuffStat = mDataSource.getSufficientStatistic(new SuffStatQueryParameter(mDataDesc.getTargetType(), targetAttribute, j));
         mClassCounts[j] = tempSuffStat.getValue().intValue();
      }

      for (int i = 0; i < numOfClassLabels; i++) {
         mNumInstances += mClassCounts[i];
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
      int[][] featureValueIndexCount = instance.getFeatureValueIndexCount();
      double[] dist = new double[mClassCounts.length];
      Arrays.fill(dist, 1.0);
      
      for (int c = 0; c < dist.length; c++) {
         for (int a = 0; a < featureValueIndexCount.length; a++) {
            for (int v = 0; v < featureValueIndexCount[a].length; v++) {
               if (featureValueIndexCount[a][v] == 0) continue;
               else if (featureValueIndexCount[a][v] == 1) {
                  dist[c] *= mCounts[a][c][v] / mAttributeClassCounts[a][c];
               } else {
                  dist[c] *= Math.pow(mCounts[a][c][v] / mAttributeClassCounts[a][c], featureValueIndexCount[a][v]);
               } 
            }
         }
         
         dist[c] *= mClassCounts[c] / mNumInstances;
      }
      
      ArrayUtil.normalize(dist);

      return dist;
   }

}
