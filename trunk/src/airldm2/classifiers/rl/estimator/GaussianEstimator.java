package airldm2.classifiers.rl.estimator;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import umontreal.iro.lecuyer.probdist.NormalDist;
import airldm2.constants.Constants;
import airldm2.core.ISufficentStatistic;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.database.rdf.SuffStatQueryParameter;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.util.MathUtil;
import airldm2.util.CollectionUtil;

public class GaussianEstimator extends AttributeEstimator {

   private Histogram mClassHistogram;
   private int mNumInstances;
   
   private Histogram mValueSums;
   private Histogram mValueSquaredSums;
   
   public GaussianEstimator(RbcAttribute att) {
      super(att);
   }

   @Override
   public void estimateParameters(RDFDataSource source, RDFDataDescriptor desc, ClassEstimator classEst) throws RDFDatabaseException {
      mClassHistogram = classEst.getClassHistogram();
      mNumInstances = classEst.getNumInstances();
      RbcAttribute targetAttribute = desc.getTargetAttribute();
      int numOfClassLabels = targetAttribute.getDomainSize();

      double[] valueSums = new double[numOfClassLabels];
      for (int j = 0; j < numOfClassLabels; j++) {
         SuffStatQueryParameter queryParam = new SuffStatQueryParameter(desc.getTargetType(), targetAttribute, j, mAttribute, -1);
         ISufficentStatistic tempSuffStat = source.getSumSufficientStatistic(queryParam);
         valueSums[j] = tempSuffStat.getValue().doubleValue();
      }
      
      mValueSums = new Histogram(valueSums);
      
      double[] valueSquaredSums = new double[numOfClassLabels];
      for (int j = 0; j < numOfClassLabels; j++) {
         SuffStatQueryParameter queryParam = new SuffStatQueryParameter(desc.getTargetType(), targetAttribute, j, mAttribute, -1);
         ISufficentStatistic tempSuffStat = source.getSquaredSumSufficientStatistic(queryParam);
         valueSquaredSums[j] = tempSuffStat.getValue().doubleValue();
      }
      
      mValueSquaredSums = new Histogram(valueSquaredSums);
   }
   
   @Override
   public boolean isValid() {
      if (mValueSums.containsZeroCount()) return false;
      
      for (int j = 0; j < mClassHistogram.size(); j++) {
         if (computeVariance(j) <= Constants.EPSILON) {
            return false;
         }
      }
      
      return true;
   }
   
   @Override
   public double score() {
      if (mClassHistogram.size() != 2) {
         throw new UnsupportedOperationException("score not supported for Gaussian estimator if there are more than two classes.");
      }
      return (computeKL(0, 1) + computeKL(1, 0)) * -0.5;     
   }
   
   private double computeKL(int class1, int class2) {
      double mean1 = computeMean(class1);
      double mean2 = computeMean(class2);
      double variance1 = computeVariance(class1);
      double variance2 = computeVariance(class2);
      return (Math.pow(mean1 - mean2, 2) + variance1 - variance2) / (2 * variance2) + Math.log(Math.sqrt(variance2 / variance1));
   }
   
   @Override
   public void mergeWith(List<AttributeEstimator> ests) {
      List<GaussianEstimator> otherEsts = CollectionUtil.makeList();
      for (AttributeEstimator est : ests) {
         if (!(est instanceof GaussianEstimator)) {
            throw new IllegalArgumentException("Expected an GaussianEstimator but " + est);
         }
         otherEsts.add((GaussianEstimator) est);
      }
      
      //Merge sums
      for (GaussianEstimator otherEst : otherEsts) {
         mClassHistogram = otherEst.mClassHistogram;
         mNumInstances = otherEst.mNumInstances;
         if (mValueSums == null) {
            mValueSums = otherEst.mValueSums.copy(); 
         } else {
            mValueSums.add(otherEst.mValueSums);
         }
      }
      
      //Merge squared sums - part 1
      for (GaussianEstimator otherEst : otherEsts) {
         if (mValueSquaredSums == null) {
            mValueSquaredSums = otherEst.mValueSquaredSums.copy(); 
         } else {
            mValueSquaredSums.add(otherEst.mValueSquaredSums);
         }
      }
      
      //Merge squared sums - part 2 (approximated by taking means)
      double[] sqSums = new double[mClassHistogram.size()];
      for (int classIndex = 0; classIndex < mClassHistogram.size(); classIndex++) {
         double sqSum = 0.0;
         for (int i = 0; i < otherEsts.size() - 1; i++) {
            GaussianEstimator estI = otherEsts.get(i);
            double meanI = estI.computeMean(classIndex);
            for (int j = i + 1; j < otherEsts.size(); j++) {
               GaussianEstimator estJ = otherEsts.get(j);
               double meanJ = estJ.computeMean(classIndex);
               sqSum +=  meanI * meanJ;
            }
         }
         sqSums[classIndex] = 2 * mClassHistogram.get(classIndex) * sqSum;
      }
      mValueSquaredSums.add(sqSums);
   }
   
   private double computeMean(int classIndex) {
      return mValueSums.get(classIndex) / mClassHistogram.get(classIndex);
   }
   
   private double computeVariance(int classIndex) {
      double mean = computeMean(classIndex);
      double sqMean = mValueSquaredSums.get(classIndex) / mClassHistogram.get(classIndex);
      return sqMean - mean * mean;
   }
   
   @Override
   public double computeLikelihood(int classIndex, AttributeValue v) {
      double val = 0.0;
      
      if (v instanceof Null) {
         return 0.0;
      } else if (!(v instanceof Numeric)) { 
         throw new IllegalArgumentException("Error: value " + v + " is not a Numeric for GaussianEstimator.");
      } else {
         val = ((Numeric) v).getValue();
      }
      
      double mean = computeMean(classIndex);
      double variance = computeVariance(classIndex);
      double density = NormalDist.density(mean, Math.sqrt(variance), val);
      double LL = Math.log(density);
      //Log.warning("class" + classIndex + " " + val + " " + mean + " " + variance + " Density=" + density + " LL=" + LL);
      return LL;
   }
   
   @Override
   public double computeLL() {
      double result = 0.0;
      for (int j = 0; j < mClassHistogram.size(); j++) {
         double N_J = mClassHistogram.get(j);
         double mean = computeMean(j);
         double variance = computeVariance(j);
         
         double[] term = new double[3];
         term[0] = -N_J * Math.log(Math.sqrt(2 * Math.PI * variance));
         term[1] = -mValueSquaredSums.get(j) / (2 * variance);
         term[2] = 0.5 * N_J * mean * mean / variance;
         
         result += MathUtil.sum(term);
         //Log.warning("class" + j + " " + N_J + " " + mean + " " + variance + " " + Arrays.toString(term));
      }
      return result;
   }

   @Override
   public double computeDualLL() {
      if (mClassHistogram.size() != 2) {
         throw new UnsupportedOperationException("DualLL not supported for Gaussian estimator if there are more than two classes.");
      }
      
      double result = 0.0;
      for (int j = 0; j < mClassHistogram.size(); j++) {
         double N_J_COMP = mClassHistogram.get(1 - j);
         double mean = computeMean(j);
         double variance = computeVariance(j);
         
         double[] term = new double[3];
         term[0] = -N_J_COMP * Math.log(Math.sqrt(2 * Math.PI * variance));
         term[1] = -mValueSquaredSums.get(1 - j) / (2 * variance);
         term[2] = 0.5 * N_J_COMP * mean * mean / (2 * variance);
         
         result += MathUtil.sum(term);
      }
      return result;
   }

   public Histogram getValueSumsForTest() {
      return mValueSums;
   }
   
   @Override
   public String toString() {
      return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
         .append("mValueSums", mValueSums)
         .append("mValueSquaredSums", mValueSquaredSums)
         .toString();
   }

   @Override
   public double paramSize() {
      return 2 * mValueSums.size();
   }

}
