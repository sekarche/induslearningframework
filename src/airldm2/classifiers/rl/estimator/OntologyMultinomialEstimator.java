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
import airldm2.util.Timer;

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
      mParameters = null;
   }
   
   @Override
   public void estimateParameters() throws RDFDatabaseException {
      RbcAttribute targetAttribute = mDesc.getTargetAttribute();
      int numOfClassLabels = targetAttribute.getDomainSize();
      computeValueHistograms(mSource, mDesc, targetAttribute);      
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

      Timer.INSTANCE.start("Query");
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
      Timer.INSTANCE.stop("Query");
   }
   
   private void computeValueHistogramsForAllHierarchy(RDFDataSource source, RDFDataDescriptor desc, RbcAttribute targetAttribute) throws RDFDatabaseException {
      int numOfClassLabels = targetAttribute.getDomainSize();
      
      if (mValueHistograms == null) {
         Timer.INSTANCE.start("Query");
         
         mValueHistograms = CollectionUtil.makeList();
         for (int j = 0; j < numOfClassLabels; j++) {
            SuffStatQueryParameter queryParam = new SuffStatQueryParameter(desc.getTargetType(), targetAttribute, j, mAttribute, -1);
            Map<URI, Double> valueHistogram = source.getMultinomialSufficientStatisticForAllHierarchy(queryParam);
            mValueHistograms.add(valueHistogram);
         }
         
         Timer.INSTANCE.stop("Query");
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
   public void estimateAllParameters() throws RDFDatabaseException {
      RbcAttribute targetAttribute = mDesc.getTargetAttribute();
      int numOfClassLabels = targetAttribute.getDomainSize();
      
      URI hierarchyRoot = mAttribute.getHierarchyRoot();
      computeValueHistogramsForAllHierarchy(mSource, mDesc, targetAttribute);
      
//      Cut cut = mTBox.getAllNodesAsCut(hierarchyRoot);
//      
//      Cut oldCut = mCut;
//      setCut(cut);
//      computeValueHistograms(mSource, mDesc, targetAttribute);

      List<List<URI>> layers = mTBox.getLayers(hierarchyRoot);
      for (int i = layers.size() - 1; i >= 0; i--) {
         List<URI> layer = layers.get(i);
         for (URI n : layer) {
            if (mTBox.isLeaf(n)) continue;
            List<URI> subclasses = mTBox.getDirectSubclass(n);
            
            for (Map<URI,Double> valueHistogram : mValueHistograms) {
               Double sum = valueHistogram.get(n);
               if (sum == null) sum = 0.0;
               sum += sum(valueHistogram, subclasses);
               
               valueHistogram.put(n, sum);
            }
         }
      }

      computeDependentParameters(numOfClassLabels);
      
//      setCut(oldCut);
   }

   public void estimateLeafParameters() throws RDFDatabaseException {
      RbcAttribute targetAttribute = mDesc.getTargetAttribute();
      
      URI hierarchyRoot = mAttribute.getHierarchyRoot();
      Cut cut = mTBox.getLeafCut(hierarchyRoot);
      Cut oldCut = mCut;
      setCut(cut);
      computeValueHistograms(mSource, mDesc, targetAttribute);
      setCut(oldCut);
   }
   
   private double sum(Map<URI, Double> valueHistogram, List<URI> subclasses) {
      double sum = 0.0;
      for (URI sub : subclasses) {
         Double v = valueHistogram.get(sub);
         if (v == null) v = 0.0;
         sum += v;
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
      
      int N = (int) (val.sum());
      Log.info(Arrays.toString(val.getIntArray(0)));
      return MathUtil.logMultinomialDist(N, mParameters[classIndex], val.getIntArray(0));
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
            
            mParameters[j][i] = (valueHistogram.get(key) + 1.0) / (classCount + valueHistogram.size());
         }
         
         MathUtil.normalize(mParameters[j]);
         Log.info(Arrays.toString(mParameters[j]));
      }
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

   public List<Map<URI, Double>> getValueHistograms() {
      return mValueHistograms;
   }

}
