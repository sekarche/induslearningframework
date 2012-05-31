package airldm2.classifiers.rl.estimator;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openrdf.model.URI;

import umontreal.iro.lecuyer.probdist.NormalDist;
import airldm2.classifiers.rl.ontology.Cut;
import airldm2.classifiers.rl.ontology.TBox;
import airldm2.constants.Constants;
import airldm2.core.ISufficentStatistic;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.database.rdf.SuffStatQueryParameter;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.util.MathUtil;
import airldm2.util.CollectionUtil;

public class OntologyGaussianFixedVarianceEstimator extends OntologyAttributeEstimator {

   //[class value]=Map
   private List<Map<URI,Double>> mValueHistograms;
   
   private Histogram mClassHistogram;
   
   private double mVariance;
   
   public OntologyGaussianFixedVarianceEstimator(TBox tBox, RbcAttribute att) {
      super(tBox, att);
      //Use difference of two means as variance
      mVariance = -1.0;
   }
   
   public OntologyGaussianFixedVarianceEstimator(TBox tBox, RbcAttribute att, double variance) {
      super(tBox, att);
      mVariance = variance;
   }
   
   @Override
   public void setCut(Cut cut) {
      super.setCut(cut);
   }
   
   @Override
   public void estimateParameters(RDFDataSource source, RDFDataDescriptor desc, ClassEstimator classEst) throws RDFDatabaseException {
      mClassHistogram = classEst.getClassHistogram();
      
      RbcAttribute targetAttribute = desc.getTargetAttribute();
      int numOfClassLabels = targetAttribute.getDomainSize();

      if (mValueHistograms == null) {
         mValueHistograms = CollectionUtil.makeList();
         for (int j = 0; j < numOfClassLabels; j++) {
            Map<URI,Double> histogram = CollectionUtil.makeMap();
            mValueHistograms.add(histogram);
         }
      }
      
      for (int j = 0; j < numOfClassLabels; j++) {
         Map<URI, Double> valueHistogram = mValueHistograms.get(j);
         for (int k = 0; k < mCut.size(); k++) {
            URI key = mCut.get().get(k);
            if (valueHistogram.containsKey(key)) continue;
            
            RbcAttribute extendedAtt = mAttribute.extendWithHierarchy(key, mTBox.isLeaf(key));
            SuffStatQueryParameter queryParam = new SuffStatQueryParameter(desc.getTargetType(), targetAttribute, j, extendedAtt, -1);
            ISufficentStatistic tempSuffStat = source.getSumSufficientStatistic(queryParam);
            double valueCount = tempSuffStat.getValue().doubleValue();
            valueHistogram.put(key, valueCount);
            
            //System.out.println(queryParam);
            //System.out.println(tempSuffStat.getValue());
         }
      }
   }

   @Override
   public void estimateAllParameters(RDFDataSource source, RDFDataDescriptor desc, ClassEstimator classEst) throws RDFDatabaseException {
   }
   
   private double sum(Map<URI, Double> valueHistogram, Cut cut) {
      double sum = 0.0;
      for (URI i : cut.get()) {
         sum += valueHistogram.get(i);
      }
      return sum;
   }

   @Override
   public boolean isValid() {
      for (int j = 0; j < mClassHistogram.size(); j++) {
         if ((sum(mValueHistograms.get(j), mCut) < Constants.EPSILON)) {
            return false;
         } else if (computeVariance(j) <= Constants.EPSILON) {
            return false;
         }
      }
      return true;
   }
   
   private double computeMean(int classIndex) {
      return sum(mValueHistograms.get(classIndex), mCut) / mClassHistogram.get(classIndex);
   }
   
   private double computeVariance(int classIndex) {
      if (mVariance >= 0.0) {
         return mVariance;
      }
      
      double meandiff = Math.abs(computeMean(1) - computeMean(0));
      return meandiff;
   }
   
   @Override
   public double computeLikelihood(int classIndex, AttributeValue v) {
      double val = 0.0;
      
      if (v instanceof Null) {
         val = 0.0;
      } else if (!(v instanceof Numeric)) { 
         throw new IllegalArgumentException("Error: value " + v + " is not a Numeric for OntologyGaussianKnownVarianceEstimator.");
      } else {
         val = ((Numeric) v).getValue();
      }
      
      double mean = computeMean(classIndex);
      double variance = computeVariance(classIndex);
      double density = NormalDist.density(mean, Math.sqrt(variance), val);
      double LL = Math.log(density);
      return LL;
   }
   
   @Override
   public double computeLL() {
      if (!isValid()) return -10000;
      
      double result = 0.0;
      for (int j = 0; j < mClassHistogram.size(); j++) {
         double N_J = mClassHistogram.get(j);
         double mean = computeMean(j);
         double variance = computeVariance(j);
         
         double[] term = new double[3];
         term[0] = -N_J * Math.log(Math.sqrt(2 * Math.PI * variance));
         term[1] = -N_J * (variance + mean * mean) / (2 * variance);
         term[2] = 0.5 * N_J * mean * mean / variance;
         
         result += MathUtil.sum(term);
      }
      return result;
   }

   @Override
   public double computeDualLL() {
      if (mClassHistogram.size() != 2) {
         throw new UnsupportedOperationException("DualLL not supported for Gaussian estimator if there are more than two classes.");
      }
      
      if (!isValid()) return -10000;
      
      double result = 0.0;
      for (int j = 0; j < mClassHistogram.size(); j++) {
         double N_J_COMP = mClassHistogram.get(1 - j);
         double mean = computeMean(j);
         double variance = computeVariance(j);
         
         double[] term = new double[3];
         term[0] = -N_J_COMP * Math.log(Math.sqrt(2 * Math.PI * variance));
         term[1] = -N_J_COMP * (variance + mean * mean) / (2 * variance);
         term[2] = 0.5 * N_J_COMP * mean * mean / (2 * variance);
         
         result += MathUtil.sum(term);
      }
      return result;
   }

   @Override
   public String toString() {
      return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
         .append("name", mAttribute.getName())   
         .append("mValueHistograms", mValueHistograms)
         .toString();
   }

   @Override
   public double paramSize() {
      int validMember = 0;
      for (URI i : mCut.get()) {
         boolean isValid = true;
         for (int j = 0; j < mValueHistograms.size(); j++) {
            if (mValueHistograms.get(j).get(i) < Constants.EPSILON) {
               isValid = false;
               break;
            }
         }
         if (isValid) validMember++;
      }
      
      return mClassHistogram.size() * validMember;
   }

}
