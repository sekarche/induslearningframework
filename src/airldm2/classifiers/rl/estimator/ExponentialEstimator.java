package airldm2.classifiers.rl.estimator;

import airldm2.core.ISufficentStatistic;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.database.rdf.SuffStatQueryParameter;
import airldm2.exceptions.RDFDatabaseException;

public class ExponentialEstimator extends AttributeEstimator {

   private Histogram mClassHistogram;
   private Histogram mValueSums;
   
   public ExponentialEstimator(RbcAttribute att) {
      super(att);
   }

   @Override
   public void estimateParameters(RDFDataSource source, RDFDataDescriptor desc, ClassEstimator classEst) throws RDFDatabaseException {
      mClassHistogram = classEst.getClassHistogram();
      RbcAttribute targetAttribute = desc.getTargetAttribute();
      int numOfClassLabels = targetAttribute.getDomainSize();

      double[] valueSums = new double[numOfClassLabels];
      for (int j = 0; j < numOfClassLabels; j++) {
         SuffStatQueryParameter queryParam = new SuffStatQueryParameter(desc.getTargetType(), targetAttribute, j, mAttribute, -1);
         ISufficentStatistic tempSuffStat = source.getSumSufficientStatistic(queryParam);
         valueSums[j] = tempSuffStat.getValue().doubleValue();
      }
      
      mValueSums = new Histogram(valueSums);
   }

   @Override
   public double computeLikelihood(int classIndex, AttributeValue v) {
      if (v instanceof Null) return 1.0;
      if (!(v instanceof Numeric)) 
         throw new IllegalArgumentException("Error: value " + v + " is not a Numeric for ExponentialEstimator.");
      
      double val = ((Numeric) v).getValue();
      double mean = mValueSums.get(classIndex) / mClassHistogram.get(classIndex);
      double lambda = 1 / mean;
      
      return lambda * Math.exp(-lambda * val);
   }
   
   public Histogram getValueSumsForTest() {
      return mValueSums;
   }

}
