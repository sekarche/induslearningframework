package airldm2.classifiers.rl.estimator;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openrdf.model.URI;

import airldm2.classifiers.rl.ontology.TBox;
import airldm2.core.ISufficentStatistic;
import airldm2.core.rl.RbcAttribute;
import airldm2.database.rdf.SuffStatQueryParameter;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.util.CollectionUtil;

public class ExponentialEstimator extends AttributeEstimator {

   private Histogram mValueSums;
   
   public ExponentialEstimator(RbcAttribute att) {
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
   }

   @Override
   public Map<URI, AttributeEstimator> makeForAllHierarchy(TBox tBox) throws RDFDatabaseException {
      Map<URI, AttributeEstimator> estimators = CollectionUtil.makeMap();
      RbcAttribute targetAttribute = mDesc.getTargetAttribute();
      int numOfClassLabels = targetAttribute.getDomainSize();
      
      List<Map<URI, Double>> hierarchyValueSums = CollectionUtil.makeList();
      Set<URI> hierarchy = CollectionUtil.makeSet();
      for (int j = 0; j < numOfClassLabels; j++) {
         SuffStatQueryParameter queryParam = new SuffStatQueryParameter(mDesc.getTargetType(), targetAttribute, j, mAttribute, -1);
         Map<URI, Double> hierarchyStat;
         
         hierarchyStat = mSource.getSumSufficientStatisticForAllHierarchy(queryParam);
         hierarchyValueSums.add(hierarchyStat);
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
         
         RbcAttribute extendedAtt = mAttribute.extendWithHierarchy(uri, tBox.isLeaf(uri));
         ExponentialEstimator est = (ExponentialEstimator) extendedAtt.getEstimator();
         est.setDataSource(mSource, mDesc, mClassEst);
         est.mValueSums = new Histogram(valueSums);
         estimators.put(uri, est);
      }
      
      return estimators;
   }
   
   @Override
   public boolean isValid() {
      return !mValueSums.containsZeroCount();
   }

   @Override
   public double score() {
      if (getClassSize() != 2) {
         throw new UnsupportedOperationException("score not supported for Exponential estimator if there are more than two classes.");
      }
      return (computeKL(0, 1) + computeKL(1, 0)) * -0.5;     
   }
   
   private double computeKL(int class1, int class2) {
      double mean1 = computeMean(class1);
      double mean2 = computeMean(class2);
      return Math.log(mean1) - Math.log(mean2) + mean2 / mean1 - 1;
   }
   
   private double computeMean(int classIndex) {
      return (mValueSums.get(classIndex) + 1) / (getClassCount(classIndex) + getClassSize());
   }
   
   @Override
   public void mergeWith(List<AttributeEstimator> ests) {
      for (AttributeEstimator est : ests) {
         if (!(est instanceof ExponentialEstimator)) {
            throw new IllegalArgumentException("Expected an ExponentialEstimator but " + est);
         }
         
         ExponentialEstimator otherEst = (ExponentialEstimator) est;
         if (mValueSums == null) {
            mValueSums = otherEst.mValueSums.copy(); 
         } else {
            mValueSums.add(otherEst.mValueSums);
         }
      }
   }
   
   @Override
   public double computeLikelihood(int classIndex, AttributeValue v) {
      double val = 0.0;
      
      if (v instanceof Null) {
         return 0.0;
      } else if (!(v instanceof Numeric)) { 
         throw new IllegalArgumentException("Error: value " + v + " is not a Numeric for ExponentialEstimator.");
      } else {
         val = ((Numeric) v).getValue();
      }
      
      double mean = computeMean(classIndex);
      double lambda = 1 / mean;
      
      return Math.log(lambda) - (lambda * val);
   }

   public Histogram getValueSumsForTest() {
      return mValueSums;
   }
   
   @Override
   public String toString() {
      return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
         .append("URI", mAttribute.getExtendedHierarchy())
         .append("mValueSums", mValueSums)
         .toString();
   }

   @Override
   public double paramSize() {
      return mValueSums.size();
   }

}
