package airldm2.classifiers.rl.estimator;

import airldm2.core.ISufficentStatistic;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.database.rdf.SuffStatQueryParameter;
import airldm2.exceptions.RDFDatabaseException;

public class MultinomialEstimator extends AttributeEstimator {
   
   //[class value][attribute value]=count
   private Histogram[] mValueHistograms;
   
   //[class value]=count
   private Histogram mClassHistogram;
   
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


   public Histogram[] getValueHistogramsForTest() {
      return mValueHistograms;
   }
   
   public Histogram getClassHistogramForTest() {
      return mClassHistogram;
   }
   
}
