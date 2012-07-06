package airldm2.classifiers.rl.estimator;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import airldm2.classifiers.rl.ontology.TBox;
import airldm2.core.ISufficentStatistic;
import airldm2.core.rl.RbcAttribute;
import airldm2.database.rdf.SuffStatQueryParameter;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.util.MathUtil;

public class BernoulliEstimator extends AttributeEstimator {
   
   //[class value][attribute value]=count
   private Histogram[] mValueHistograms;
   
   //[class value]=count
   private Histogram mClassHistogram;
   
   public BernoulliEstimator(RbcAttribute att) {
      super(att);
   }
   
   @Override
   public void estimateParameters() throws RDFDatabaseException {
      RbcAttribute targetAttribute = mDesc.getTargetAttribute();
      int numOfClassLabels = targetAttribute.getDomainSize();
      int numOfAttributeValues = mAttribute.getDomainSize();

      mValueHistograms = new Histogram[numOfClassLabels];
      for (int j = 0; j < numOfClassLabels; j++) {
         double[] valueCounts = new double[numOfAttributeValues];
         for (int k = 0; k < numOfAttributeValues; k++) {
            SuffStatQueryParameter queryParam = new SuffStatQueryParameter(mDesc.getTargetType(), targetAttribute, j, mAttribute, k);
            ISufficentStatistic tempSuffStat = mSource.getBernoulliSufficientStatistic(queryParam);
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
   public boolean isValid() {
      return true;
   }

   @Override
   public void mergeWith(List<AttributeEstimator> ests) {
      throw new NotImplementedException();
   }

   @Override
   public double computeLikelihood(int classIndex, AttributeValue v) {
      if (v instanceof Null) return 1.0;
      if (!(v instanceof Histogram)) 
         throw new IllegalArgumentException("Error: value " + v + " is not a Histogram for BernoulliEstimator.");
      
      initLikelihood();
      
      Histogram val = (Histogram) v;
      Log.info(Arrays.toString(val.getIntArray(0)));
      return MathUtil.logBernoulliDist(mParameters[classIndex], val.getIntArray(0));
   }
   
   private double[][] mParameters;
   private void initLikelihood() {
      if (mParameters != null) return;
      
      mParameters = new double[mClassHistogram.size()][];
      for (int j = 0; j < mClassHistogram.size(); j++) {
         Histogram valueHistogram = mValueHistograms[j];
         final double classCount = mClassHistogram.get(j);
         mParameters[j] = new double[valueHistogram.size()];
         for (int i = 0; i < mParameters[j].length; i++) {
            mParameters[j][i] = (valueHistogram.get(i) + 1.0) / (classCount + 2.0);
         }
         
         Log.info(Arrays.toString(mParameters[j]));
      }
   }

   public Histogram[] getValueHistogramsForTest() {
      return mValueHistograms;
   }
   
   public Histogram getClassHistogramForTest() {
      return mClassHistogram;
   }

   @Override
   public double paramSize() {
      return mClassHistogram.size() * mValueHistograms[0].size();
   }

   @Override
   public Map<URI, AttributeEstimator> makeForAllHierarchy(TBox tBox) {
      return null;
   }

}
