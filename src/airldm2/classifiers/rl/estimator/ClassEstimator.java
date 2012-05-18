package airldm2.classifiers.rl.estimator;

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
      
      for (int i = 0; i < numOfClassLabels; i++) {
         mNumInstances += (int) classCounts[i];
      }
   }
   
   public double computeLikelihood(int classIndex) {
      //With Laplace correction
      return (double)(mClassHistogram.get(classIndex) + 1.0) / (mNumInstances + mClassHistogram.size());
   }
   
   public Histogram getClassHistogram() {
      return mClassHistogram;
   }
   
   public int getNumInstances() {
      return mNumInstances;
   }
   
}
