package airldm2.classifiers.rl.estimator;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openrdf.model.URI;

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

public class OntologyBernoulliEstimator extends OntologyAttributeEstimator {

   //[class value]=Map
   private List<Map<URI,Double>> mValueHistograms;
   
   private Histogram mClassHistogram;
   
   public OntologyBernoulliEstimator(TBox tBox, RbcAttribute att) {
      super(tBox, att);
   }
   
   @Override
   public void setCut(Cut cut) {
      super.setCut(cut);
      OntologyEnumType cutEnum = new OntologyEnumType(mTBox, cut.get());
      mAttribute.setValueType(cutEnum);
      mParameters = null;
   }
   
   @Override
   public void estimateParameters() throws RDFDatabaseException {
      mClassHistogram = mClassEst.getClassHistogram();
      RbcAttribute targetAttribute = mDesc.getTargetAttribute();
      computeValueHistograms(mSource, mDesc, targetAttribute);
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
            ISufficentStatistic tempSuffStat = source.getBernoulliSufficientStatistic(queryParam);
            double valueCount = tempSuffStat.getValue().intValue();
            valueHistogram.put(key, valueCount);
            
            //System.out.println(queryParam);
            //System.out.println(tempSuffStat.getValue());
         }
      }
   }

   @Override
   public void estimateAllParameters() throws RDFDatabaseException {
   }

   @Override
   public boolean isValid() {
      return true;
   }
   
   @Override
   public double computeLikelihood(int classIndex, AttributeValue v) {
      if (v instanceof Null) return 1.0;
      if (!(v instanceof Histogram)) 
         throw new IllegalArgumentException("Error: value " + v + " is not a Histogram for OntologyBernoulliEstimator.");
      
      Histogram val = null;
      if (v instanceof MappedHistogram) {
         List<URI> domain = mCut.get();
         MappedHistogram allV = (MappedHistogram) v;
         val = allV.induce(domain);
      } else {
         val = (Histogram) v;
      }
      
      if (val.size() != mCut.size())
         throw new IllegalArgumentException("Error: size of " + v + " (" + val.size() + ") does not match the size of current cut (" + mCut.size() + ").");

      initLikelihood();

      Log.info(Arrays.toString(val.getIntArray(0)));
      return MathUtil.logBernoulliDist(mParameters[classIndex], val.getIntArray(0));
   }

   private double[][] mParameters;
   private void initLikelihood() {
      if (mParameters != null) return;
      
      mParameters = new double[mClassHistogram.size()][];
      for (int j = 0; j < mClassHistogram.size(); j++) {
         Map<URI,Double> valueHistogram = mValueHistograms.get(j);
         final double classCount = mClassHistogram.get(j);
         mParameters[j] = new double[mCut.size()];
         for (int i = 0; i < mParameters[j].length; i++) {
            URI key = mCut.get().get(i);
            
            mParameters[j][i] = (valueHistogram.get(key) + 1.0) / (classCount + 2.0);
         }
         
         Log.info(Arrays.toString(mParameters[j]));
      }
   }

   @Override
   public String toString() {
      return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
         .append("name", mAttribute.getName())   
         .append("mValueHistograms", mValueHistograms)
         .append("mClassHistogram", mClassHistogram)
         .toString();
   }

   @Override
   public double paramSize() {
      return mClassHistogram.size() * mCut.size();
   }

}
