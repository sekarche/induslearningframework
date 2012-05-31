package airldm2.classifiers.rl.estimator;

import static airldm2.constants.Constants.EPSILON;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openrdf.model.URI;

import umontreal.iro.lecuyer.probdistmulti.MultinomialDist;
import umontreal.iro.lecuyer.util.Num;
import airldm2.classifiers.rl.ontology.Cut;
import airldm2.classifiers.rl.ontology.TBox;
import airldm2.core.ISufficentStatistic;
import airldm2.core.rl.OntologyEnumType;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.database.rdf.SuffStatQueryParameter;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.util.CollectionUtil;
import airldm2.util.MathUtil;
import cern.colt.Arrays;

public class OntologyMultinomialEstimator extends OntologyAttributeEstimator {

   //[class value]=Map
   private List<Map<URI,Double>> mValueHistograms;
   
   //[class value]=count
   private Histogram mClassHistogram;
   
   //[attribute value]=Total
   private Map<URI,Double> mValueHistogram;
   
   private double mTotal;
   
   public OntologyMultinomialEstimator(TBox tBox, RbcAttribute att) {
      super(tBox, att);
   }
   
   @Override
   public void setCut(Cut cut) {
      super.setCut(cut);
      OntologyEnumType cutEnum = new OntologyEnumType(mTBox, cut.get());
      mAttribute.setValueType(cutEnum);
   }
   
   @Override
   public void estimateParameters(RDFDataSource source, RDFDataDescriptor desc, ClassEstimator classEst) throws RDFDatabaseException {
      RbcAttribute targetAttribute = desc.getTargetAttribute();
      int numOfClassLabels = targetAttribute.getDomainSize();
      computeValueHistograms(source, desc, targetAttribute);      
      computeDependentParameters(numOfClassLabels);
   }

   private void computeValueHistograms(RDFDataSource source, RDFDataDescriptor desc, RbcAttribute targetAttribute) throws RDFDatabaseException {
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
   }

   private void computeDependentParameters(int numOfClassLabels) {
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
   public void estimateAllParameters(RDFDataSource source, RDFDataDescriptor desc, ClassEstimator classEst) throws RDFDatabaseException {
      RbcAttribute targetAttribute = desc.getTargetAttribute();
      int numOfClassLabels = targetAttribute.getDomainSize();
      
      URI hierarchyRoot = mAttribute.getHierarchyRoot();
      Cut cut = mTBox.getLeafCut(hierarchyRoot);
      Cut oldCut = mCut;
      setCut(cut);
      computeValueHistograms(source, desc, targetAttribute);
      
      while ((cut = cut.abstractCut()) != null) {
         for (URI sup : cut.get()) {
            if (mValueHistograms.get(0).containsKey(sup)) continue;
            
            List<URI> subclasses = mTBox.getDirectSubclass(sup);
            for (Map<URI,Double> valueHistogram : mValueHistograms) {
               double sumSubclasses = sum(valueHistogram, subclasses);
               valueHistogram.put(sup, sumSubclasses);
            }
         }
      }
      
      computeDependentParameters(numOfClassLabels);
      setCut(oldCut);
   }
   
   private double sum(Map<URI, Double> valueHistogram, List<URI> subclasses) {
      double sum = 0.0;
      for (URI sub : subclasses) {
         sum += valueHistogram.get(sub);
      }
      return sum;
   }

   @Override
   public boolean isValid() {
      return true;
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
      
      int N = (int) (val.sum() + val.size());
      double[] p = new double[val.size()];
      for (int i = 0; i < val.size(); i++) {
         URI key = mCut.get().get(i);
         
         p[i] = (valueHistogram.get(key) + 1.0) / (classCount + valueHistogram.size());
      }
      
      MathUtil.normalize(p);
      Log.warning(Arrays.toString(p));
      
      return Math.log(MultinomialDist.prob(N, p, val.getIntArray(1)));
   }

   @Override
   public double computeLL() {
      double result = 0.0;
      for (int j = 0; j < mClassHistogram.size(); j++) {
         double sumN_JK = 0.0;
         
         for (int k = 0; k < mCut.size(); k++) {
            URI key = mCut.get().get(k);
            final double N_JK = mValueHistograms.get(j).get(key);
            sumN_JK += N_JK;
            
            final double N_J = mClassHistogram.get(j);
            if (N_JK < EPSILON || N_J < EPSILON) continue;
            
            Log.info(j + " " + k + " " + key + " " + N_JK + " " + N_J);
            result += N_JK * Math.log(N_JK / N_J);
            result -= Num.lnFactorial((int)N_JK);
         }
         result += Num.lnFactorial((int)sumN_JK);
      }
      return result;
   }

   @Override
   public double computeDualLL() {
      double result = 0.0;
      for (int j = 0; j < mClassHistogram.size(); j++) {
         double sumN_JK = 0.0;
         
         for (int k = 0; k < mCut.size(); k++) {
            URI key = mCut.get().get(k);
            final double N_JK = mValueHistograms.get(j).get(key);
            sumN_JK += N_JK;
            
            final double NUM = mValueHistogram.get(key) - N_JK;
            final double DEN = mTotal - mClassHistogram.get(j);
            if (NUM < EPSILON || DEN < EPSILON) continue;
            result += N_JK * Math.log(NUM / DEN);
            result -= Num.lnFactorial((int)N_JK);
         }
         result += Num.lnFactorial((int)sumN_JK);
      }
      return result;
   }

   @Override
   public String toString() {
      return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
         .append("name", mAttribute.getName())   
         .append("mValueHistograms", mValueHistograms)
         .append("mClassHistogram", mClassHistogram)
         .append("mValueHistogram", mValueHistogram)
         .append("mTotal", mTotal)
         .toString();
   }

   @Override
   public double paramSize() {
      return mClassHistogram.size() * mCut.size();
   }

}
