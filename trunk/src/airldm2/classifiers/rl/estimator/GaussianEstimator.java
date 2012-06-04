package airldm2.classifiers.rl.estimator;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openrdf.model.URI;

import umontreal.iro.lecuyer.probdist.NormalDist;
import airldm2.classifiers.rl.ontology.TBox;
import airldm2.constants.Constants;
import airldm2.core.ISufficentStatistic;
import airldm2.core.rl.BinnedType;
import airldm2.core.rl.RbcAttribute;
import airldm2.database.rdf.SuffStatQueryParameter;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.util.CollectionUtil;
import airldm2.util.MathUtil;

public class GaussianEstimator extends AttributeEstimator {

   private Histogram mValueSums;
   private Histogram mValueSquaredSums;
   
   public GaussianEstimator(RbcAttribute att) {
      super(att);
   }

   @Override
   public void estimateParameters() throws RDFDatabaseException {
      RbcAttribute targetAttribute = mDesc.getTargetAttribute();
      int numOfClassLabels = targetAttribute.getDomainSize();

      double[] valueSums = new double[numOfClassLabels];
      for (int j = 0; j < numOfClassLabels; j++) {
         SuffStatQueryParameter queryParam = new SuffStatQueryParameter(mDesc.getTargetType(), targetAttribute, j, mAttribute, -1);
         ISufficentStatistic tempSuffStat = mSource.getSumSufficientStatistic(queryParam);
         valueSums[j] = tempSuffStat.getValue().doubleValue();
      }
      
      mValueSums = new Histogram(valueSums);
      
      double[] valueSquaredSums = new double[numOfClassLabels];
      for (int j = 0; j < numOfClassLabels; j++) {
         SuffStatQueryParameter queryParam = new SuffStatQueryParameter(mDesc.getTargetType(), targetAttribute, j, mAttribute, -1);
         ISufficentStatistic tempSuffStat = mSource.getSquaredSumSufficientStatistic(queryParam);
         valueSquaredSums[j] = tempSuffStat.getValue().doubleValue();
      }
      
      mValueSquaredSums = new Histogram(valueSquaredSums);
   }


   @Override
   public Map<URI, AttributeEstimator> makeForAllHierarchy(TBox tBox) throws RDFDatabaseException {
      Map<URI, AttributeEstimator> estimators = CollectionUtil.makeMap();
      RbcAttribute targetAttribute = mDesc.getTargetAttribute();
      int numOfClassLabels = targetAttribute.getDomainSize();
      
      List<Map<URI, Double>> hierarchyValueSums = CollectionUtil.makeList();
      List<Map<URI, Double>> hierarchyValueSquaredSums = CollectionUtil.makeList();
      Set<URI> hierarchy = CollectionUtil.makeSet();
      for (int j = 0; j < numOfClassLabels; j++) {
         SuffStatQueryParameter queryParam = new SuffStatQueryParameter(mDesc.getTargetType(), targetAttribute, j, mAttribute, -1);
         Map<URI, Double> hierarchyStat;
         
         hierarchyStat = mSource.getSumSufficientStatisticForAllHierarchy(queryParam);
         hierarchyValueSums.add(hierarchyStat);
         hierarchy.addAll(hierarchyStat.keySet());
         
         hierarchyStat = mSource.getSquaredSumSufficientStatisticForAllHierarchy(queryParam);
         hierarchyValueSquaredSums.add(hierarchyStat);
         hierarchy.addAll(hierarchyStat.keySet());
      }
      
      for (URI uri : hierarchy) {
         double[] valueSums = new double[numOfClassLabels];
         for (int j = 0; j < numOfClassLabels; j++) {
            Double v = hierarchyValueSums.get(j).get(uri);
            if (v != null) {
               valueSums[j] = v;
            }
         }
         
         double[] valueSquaredSums = new double[numOfClassLabels];
         for (int j = 0; j < numOfClassLabels; j++) {
            Double v = hierarchyValueSquaredSums.get(j).get(uri);
            if (v != null) {
               valueSquaredSums[j] = v;
            }
         }
         
         RbcAttribute extendedAtt = mAttribute.extendWithHierarchy(uri, tBox.isLeaf(uri));
         GaussianEstimator est = (GaussianEstimator) extendedAtt.getEstimator();
         est.setDataSource(mSource, mDesc, mClassEst);
         est.mValueSums = new Histogram(valueSums);
         est.mValueSquaredSums = new Histogram(valueSquaredSums);
         estimators.put(uri, est);
      }
      
      return estimators;
   }

   @Override
   public boolean isValid() {
      if (mValueSums.containsZeroCount()) return false;
      
      for (int j = 0; j < getClassSize(); j++) {
         if (computeVariance(j) <= Constants.EPSILON) {
            return false;
         }
      }
      
      return true;
   }
   
   @Override
   public double score() {
      if (getClassSize() != 2) {
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
      
      if (mValueSums == null) {
         mValueSums = new Histogram(getClassSize());
      }
      if (mValueSquaredSums == null) {
         mValueSquaredSums = new Histogram(getClassSize());
      }
      
      //Merge sums
      for (GaussianEstimator otherEst : otherEsts) {
         mValueSums.add(otherEst.mValueSums);
      }
      
      //Merge squared sums - part 1
      for (GaussianEstimator otherEst : otherEsts) {
         mValueSquaredSums.add(otherEst.mValueSquaredSums);
      }
      
      //Merge squared sums - part 2 (approximated by taking means)
      double[] sqSums = new double[getClassSize()];
      for (int classIndex = 0; classIndex < getClassSize(); classIndex++) {
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
         sqSums[classIndex] = 2 * getClassCount(classIndex) * sqSum;
      }
      mValueSquaredSums.add(sqSums);
   }
   
   public double computeMean(int classIndex) {
      return mValueSums.get(classIndex) / getClassCount(classIndex);
   }
   
   private double computeVariance(int classIndex) {
      double mean = computeMean(classIndex);
      double sqMean = mValueSquaredSums.get(classIndex) / getClassCount(classIndex);
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
      Log.info("class" + classIndex + " " + val + " " + mean + " " + variance + " Density=" + density + " LL=" + LL);
      return LL;
   }
   
   @Override
   public double computeLL() {
      double result = 0.0;
      for (int j = 0; j < getClassSize(); j++) {
         double N_J = getClassCount(j);
         double mean = computeMean(j); 
         double variance = computeVariance(j);
         
         double[] term = new double[3];
         term[0] = -N_J * Math.log(Math.sqrt(2 * Math.PI * variance));
         term[1] = -mValueSquaredSums.get(j) / (2 * variance);
         term[2] = 0.5 * N_J * mean * mean / variance;
         
         result += MathUtil.sum(term);
         Log.info("class" + j + " " + N_J + " " + mean + " " + variance + " " + Arrays.toString(term));
      }
      return result;
   }

   @Override
   public double computeDualLL() {
      if (getClassSize() != 2) {
         throw new UnsupportedOperationException("DualLL not supported for Gaussian estimator if there are more than two classes.");
      }
      
      double result = 0.0;
      for (int j = 0; j < getClassSize(); j++) {
         double N_J_COMP = getClassCount(1 - j);
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
         .append("URI", mAttribute.getExtendedHierarchy())
         .append("mValueSums", mValueSums)
         .append("mValueSquaredSums", mValueSquaredSums)
         .toString();
   }

   @Override
   public double paramSize() {
      return 2 * mValueSums.size();
   }
   
   public RbcAttribute makeBinaryBinnedAttribute() {
      RbcAttribute att = mAttribute.copy();
      double[] means = new double[] {computeMean(0), computeMean(1)};
      double meanmean = MathUtil.sum(means) / 2;
      att.setValueType(new BinnedType(new double[] {meanmean}));
      return att;
   }
   
}
