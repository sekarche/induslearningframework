package airldm2.classifiers.rl.estimator;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import airldm2.core.ISufficentStatistic;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.database.rdf.SuffStatQueryParameter;
import airldm2.exceptions.RDFDatabaseException;


public class ClassEstimator {
   
   private Histogram mClassHistogram;
   private int mNumInstances;
   
   public void estimateParameters(RDFDataSource source, RDFDataDescriptor desc) throws RDFDatabaseException {
      //Explicitly ask for class count since every attribute may be HISTOGRAM
      RbcAttribute targetAttribute = desc.getTargetAttribute();
      int numOfClassLabels = targetAttribute.getDomainSize();      
      
      double[] classCounts = new double[numOfClassLabels];
      for (int j = 0; j < numOfClassLabels; j++) {
         ISufficentStatistic tempSuffStat = source.getMultinomialSufficientStatistic(new SuffStatQueryParameter(desc.getTargetType(), targetAttribute, j));
         classCounts[j] = tempSuffStat.getValue().intValue();
      }
      mClassHistogram = new Histogram(classCounts);
      
      mNumInstances = (int) mClassHistogram.sum();
   }
   
   public double computeLikelihood(int classIndex) {
      //With Laplace correction
      return Math.log((mClassHistogram.get(classIndex) + 1.0) / (mNumInstances + mClassHistogram.size()));
   }
   
   private Double mLL;
   public double computeLL() {
      if (mLL == null) {
         mLL = 0.0;
         for (int j = 0; j < mClassHistogram.size(); j++) {
            mLL += mClassHistogram.get(j) * Math.log(mClassHistogram.get(j) / mNumInstances);
         }
      }
      return mLL;
   }
   
   private Double mDualLL;
   public double computeDualLL() {
      if (mDualLL == null) {
         mDualLL = 0.0;
         for (int j = 0; j < mClassHistogram.size(); j++) {
            mDualLL += mClassHistogram.get(j) * Math.log((mNumInstances - mClassHistogram.get(j)) / mNumInstances);
         }
      }
      return mDualLL;
   }
   
   public Histogram getClassHistogram() {
      return mClassHistogram;
   }
   
   public int getNumInstances() {
      return mNumInstances;
   }

   public double paramSize() {
      return mClassHistogram.size();
   }
   
   @Override
   public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
   }
   
}
