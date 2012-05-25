package airldm2.classifiers.rl.estimator;

import static airldm2.constants.Constants.EPSILON;
import airldm2.core.ISufficentStatistic;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.database.rdf.SuffStatQueryParameter;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.util.MathUtil;

public class MultinomialEstimator extends AttributeEstimator {
   
   //[class value][attribute value]=count
   private Histogram[] mValueHistograms;
   
   //[class value]=count
   private Histogram mClassHistogram;
   
   //[attribute value]=count
   private Histogram mValueHistogram;
   
   private double mTotal;
   
   public MultinomialEstimator(RbcAttribute att) {
      super(att);
   }
   
   @Override
   public void estimateParameters(RDFDataSource source, RDFDataDescriptor desc, ClassEstimator classEst) throws RDFDatabaseException {
      RbcAttribute targetAttribute = desc.getTargetAttribute();
      int numOfClassLabels = targetAttribute.getDomainSize();
      int numOfAttributeValues = mAttribute.getDomainSize();

      mValueHistograms = new Histogram[numOfClassLabels];
      for (int j = 0; j < numOfClassLabels; j++) {
         double[] valueCounts = new double[numOfAttributeValues];
         for (int k = 0; k < numOfAttributeValues; k++) {
            SuffStatQueryParameter queryParam = new SuffStatQueryParameter(desc.getTargetType(), targetAttribute, j, mAttribute, k);
            ISufficentStatistic tempSuffStat = source.getMultinomialSufficientStatistic(queryParam);
            valueCounts[k] = tempSuffStat.getValue().intValue();
            
            //System.out.println(queryParam);
            //System.out.println(tempSuffStat.getValue());
         }
         mValueHistograms[j] = new Histogram(valueCounts);
      }
      
      double[] classCounts = new double[numOfClassLabels];
      for (int j = 0; j < numOfClassLabels; j++) {
         classCounts[j] = mValueHistograms[j].sum();
      }
      mClassHistogram = new Histogram(classCounts);
      
      mValueHistogram = MathUtil.sumAcross(mValueHistograms);
      
      mTotal = mClassHistogram.sum();
   }

   @Override
   public void mergeWith(AttributeEstimator est) {
      if (!(est instanceof MultinomialEstimator)) {
         throw new IllegalArgumentException("Expected an MultinomialEstimator but " + est);
      }
      
      MultinomialEstimator otherEst = (MultinomialEstimator) est;
      
      for (int i = 0; i < mValueHistograms.length; i++) {
         mValueHistograms[i].add(otherEst.mValueHistograms[i]);
      }
      mClassHistogram.add(otherEst.mClassHistogram);
      mValueHistogram.add(otherEst.mValueHistogram);
      mTotal += otherEst.mTotal;
   }

   @Override
   public double computeLikelihood(int classIndex, AttributeValue v) {
      if (v instanceof Null) return 1.0;
      if (!(v instanceof Histogram)) 
         throw new IllegalArgumentException("Error: value " + v + " is not a Histogram for MultinomialEstimator.");
      
      Histogram val = (Histogram) v;
      Histogram valueHistogram = mValueHistograms[classIndex];
      final double classCount = mClassHistogram.get(classIndex);
      double likelihood = 1.0;
      for (int i = 0; i < val.size(); i++) {
         if (val.get(i) == 0) continue;

         //With Laplace correction
         double pVpC = (double) (valueHistogram.get(i) + 1.0) / (classCount + valueHistogram.size());
         
         if (val.get(i) == 1) {
            likelihood *= pVpC;
         } else {
            likelihood *= Math.pow(pVpC, val.get(i));
         }
      }
      return likelihood;
   }
   
   @Override
   public double computeLL() {
      double result = 0.0;
      for (int j = 0; j < mValueHistograms.length; j++) {
         for (int k = 0; k < mValueHistograms[j].size(); k++) {
            final double N_JK = mValueHistograms[j].get(k);
            final double N_J = mClassHistogram.get(j);
            if (N_JK < EPSILON || N_J < EPSILON) continue;
            result += N_JK * MathUtil.lg(N_JK / N_J);
         }
      }
      Log.info(String.valueOf(result));
      return result;
   }

   @Override
   public double computeDualLL() {
      double result = 0.0;
      for (int j = 0; j < mValueHistograms.length; j++) {
         for (int k = 0; k < mValueHistograms[j].size(); k++) {
            final double N_JK = mValueHistograms[j].get(k);
            final double NUM = mValueHistogram.get(k) - N_JK;
            final double DEN = mTotal - mClassHistogram.get(j);
            if (NUM < EPSILON || DEN < EPSILON) continue;
            result += N_JK * MathUtil.lg(NUM / DEN);
         }
      }
      Log.info(String.valueOf(result));
      return result;
   }
   
   public Histogram[] getValueHistogramsForTest() {
      return mValueHistograms;
   }
   
   public Histogram getClassHistogramForTest() {
      return mClassHistogram;
   }

}
