package airldm2.classifiers.rl.estimator;

import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;

import airldm2.classifiers.rl.ontology.Cut;
import static airldm2.constants.Constants.EPSILON;
import airldm2.core.ISufficentStatistic;
import airldm2.core.rl.EnumType;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.database.rdf.SuffStatQueryParameter;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.util.CollectionUtil;
import airldm2.util.MathUtil;

public class OntologyMultinomialEstimator extends OntologyAttributeEstimator {

   //[class value]=Map
   private List<Map<URI,Double>> mValueHistograms;
   
   //[class value]=count
   private Histogram mClassHistogram;
   
   //[attribute value]=Total
   private Map<URI,Double> mValueHistogram;
   
   private double mTotal;
   
   public OntologyMultinomialEstimator(RbcAttribute att) {
      super(att);
   }
   
   @Override
   public void setCut(Cut cut) {
      super.setCut(cut);
      EnumType cutEnum = new EnumType(cut.get());
      mAttribute.setValueType(cutEnum);
   }
   
   @Override
   public void estimateParameters(RDFDataSource source, RDFDataDescriptor desc, ClassEstimator classEst) throws RDFDatabaseException {
      RbcAttribute targetAttribute = desc.getTargetAttribute();
      int numOfClassLabels = targetAttribute.getDomainSize();
      int numOfAttributeValues = mAttribute.getDomainSize();
      
      if (mValueHistograms == null) {
         mValueHistograms = CollectionUtil.makeList();
         for (int j = 0; j < numOfClassLabels; j++) {
            Map<URI,Double> histogram = CollectionUtil.makeMap();
            mValueHistograms.add(histogram);
         }
      }
      
      for (int j = 0; j < numOfClassLabels; j++) {
         Map<URI, Double> valueHistogram = mValueHistograms.get(j);
         for (int k = 0; k < numOfAttributeValues; k++) {
            URI key = mCut.get().get(k);
            if (valueHistogram.containsKey(key)) continue;
            
            SuffStatQueryParameter queryParam = new SuffStatQueryParameter(desc.getTargetType(), targetAttribute, j, mAttribute, k);
            ISufficentStatistic tempSuffStat = source.getMultinomialSufficientStatistic(queryParam);
            double valueCount = tempSuffStat.getValue().intValue();
            valueHistogram.put(key, valueCount);
            
            //System.out.println(queryParam);
            //System.out.println(tempSuffStat.getValue());
         }
      }
      
      if (mClassHistogram == null) {
         double[] classCounts = new double[numOfClassLabels];
         for (int j = 0; j < numOfClassLabels; j++) {
            URI hierarchyRoot = mAttribute.getHierarchyRoot();
            classCounts[j] = mValueHistograms.get(j).get(hierarchyRoot);
         }
         mClassHistogram = new Histogram(classCounts);
      }
            
      mValueHistogram = MathUtil.sumAcross(mValueHistograms);
      
      mTotal = mClassHistogram.sum();
   }

   @Override
   public double computeLikelihood(int classIndex, AttributeValue v) {
      if (v instanceof Null) return 1.0;
      if (!(v instanceof Histogram)) 
         throw new IllegalArgumentException("Error: value " + v + " is not a Histogram for MultinomialEstimator.");
      
      Histogram val = (Histogram) v;
      if (val.size() != mCut.size())
         throw new IllegalArgumentException("Error: size of " + v + " (" + val.size() + ") does not match the size of current cut (" + mCut.size() + ").");
      
      Map<URI,Double> valueHistogram = mValueHistograms.get(classIndex);
      final double classCount = mClassHistogram.get(classIndex);
      double likelihood = 1.0;
      for (int i = 0; i < val.size(); i++) {
         if (val.get(i) == 0) continue;

         URI key = mCut.get().get(i);
         //With Laplace correction
         double pVpC = (double) (valueHistogram.get(key) + 1.0) / (classCount + valueHistogram.size());
         
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
      for (int j = 0; j < mClassHistogram.size(); j++) {
         for (int k = 0; k < mCut.size(); k++) {
            URI key = mCut.get().get(k);
            final double N_JK = mValueHistograms.get(j).get(key);
            final double N_J = mClassHistogram.get(j);
            if (N_JK < EPSILON || N_J < EPSILON) continue;
            result += N_JK * MathUtil.lg(N_JK / N_J);
         }
      }
      return result;
   }

   @Override
   public double computeDualLL() {
      double result = 0.0;
      for (int j = 0; j < mClassHistogram.size(); j++) {
         for (int k = 0; k < mCut.size(); k++) {
            URI key = mCut.get().get(k);
            final double N_JK = mValueHistograms.get(j).get(key);
            final double NUM = mValueHistogram.get(key) - N_JK;
            final double DEN = mTotal - mClassHistogram.get(j);
            if (NUM < EPSILON || DEN < EPSILON) continue;
            result += N_JK * MathUtil.lg(NUM / DEN);
         }
      }
      return result;
   }
   
}
