package airldm2.classifiers.rl.tree;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import airldm2.classifiers.rl.estimator.ClassEstimator;
import airldm2.classifiers.rl.estimator.Histogram;
import airldm2.core.ISufficentStatistic;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.core.rl.RbcAttributeValue;
import airldm2.database.rdf.TreePathQueryParameter;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.util.CollectionUtil;
import airldm2.util.MathUtil;

public class TreeNodeSplitter {
   
   //[class value][attribute value]=count
   private Histogram[] mValueHistograms;
   
   //[class value]=count
   private Histogram mClassHistogram;
   
   //[attribute value]=count
   private Histogram mValueHistogram;
   
   private double mTotal;

   private final RDFDataSource mSource;
   private final RDFDataDescriptor mDesc;
   private final ClassEstimator mClassEst;
   private final RbcAttributeValue mAttValue;
   
   public TreeNodeSplitter(RDFDataSource dataSource, RDFDataDescriptor dataDesc, ClassEstimator classEst, RbcAttributeValue attValue) {
      mSource = dataSource;
      mDesc = dataDesc;
      mClassEst = classEst;
      mAttValue = attValue;
   }
   
   public RbcAttributeValue getAttributeValue() {
      return mAttValue;
   }
   
   public void estimateParameters(List<TreeNodeSplitter> ancestors, List<TreeEdge> pathEdges) throws RDFDatabaseException {
      RbcAttribute targetAttribute = mDesc.getTargetAttribute();
      int numOfClassLabels = targetAttribute.getDomainSize();

      computeClassHistogram(ancestors, pathEdges, numOfClassLabels);
      
      List<RbcAttributeValue> ancestorAtts = getAttributeValueList(ancestors);
      
      mValueHistograms = new Histogram[numOfClassLabels];
      for (int j = 0; j < numOfClassLabels; j++) {
         double[] valueCounts = new double[2];
         
         TreePathQueryParameter queryParam = new TreePathQueryParameter(mDesc.getTargetType(), targetAttribute, j, ancestorAtts, pathEdges, mAttValue);
         ISufficentStatistic tempSuffStat = mSource.getTreePathSufficientStatistic(queryParam);
         valueCounts[0] = tempSuffStat.getValue().intValue();
         //System.out.println(queryParam);
         //System.out.println(tempSuffStat.getValue());
         
         valueCounts[1] = mClassHistogram.get(j) - valueCounts[0];
         
         mValueHistograms[j] = new Histogram(valueCounts);
      }
      
      mValueHistogram = MathUtil.sumAcross(mValueHistograms);
      
      mTotal = mClassHistogram.sum();
   }
   
   private void computeClassHistogram(List<TreeNodeSplitter> ancestors, List<TreeEdge> pathEdges, int numOfClassLabels) {
      if (ancestors.isEmpty()) {
         mClassHistogram = mClassEst.getClassHistogram().copy();
         
      } else {
         double[] classCounts = new double[numOfClassLabels];
         TreeNodeSplitter parent = ancestors.get(ancestors.size() - 1);
         TreeEdge parentEdge = pathEdges.get(pathEdges.size() - 1);
         int edgeIndex = parentEdge.Value ? 0 : 1;
         
         for (int j = 0; j < numOfClassLabels; j++) {
            classCounts[j] = parent.mValueHistograms[j].get(edgeIndex);
         }
         mClassHistogram = new Histogram(classCounts);
      }
   }

   public static List<RbcAttributeValue> getAttributeValueList(List<TreeNodeSplitter> as) {
      List<RbcAttributeValue> list = CollectionUtil.makeList();
      for (TreeNodeSplitter a : as) {
         list.add(a.getAttributeValue());
      }
      return list;
   }

   public double getInfoGain() {
      if (mTotal <= 0.0) return 0.0;
      
      double classEntropy = mClassHistogram.getEntropy();
      double remainder = 0.0;
      for (int i = 0; i < mValueHistogram.size(); i++) {
         double[] valueHistogram = new double[mClassHistogram.size()];
         for (int j = 0; j < mClassHistogram.size(); j++) {
            valueHistogram[j] = mValueHistograms[j].get(i);
         }
         double entropy = MathUtil.getEntropy(valueHistogram);
         remainder += entropy * mValueHistogram.get(i) / mTotal;
      }
      return classEntropy - remainder;
   }
   
   public double getTruePositiveCount(Boolean v) {
      int prediction = getPredictionAtLeaf(v);
      int i = v ? 0 : 1;
      return mValueHistograms[prediction].get(i);
   }
   
   public int getPredictionAtLeaf(Boolean v) {
      int i = v ? 0 : 1;
      double[] vs = new double[mClassHistogram.size()];
      for (int j = 0; j < vs.length; j++) {
         vs[j] = mValueHistograms[j].get(i);
      }
      return MathUtil.maxIndex(vs);
   }

   @Override
   public String toString() {
      return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
         .append("Attribute", mAttValue.Attribute.getName())
         .append("AttributeValue", mAttValue.ValueKey)
         .append("mValueHistograms", mValueHistograms)
         .append("mClassHistogram", mClassHistogram)
         .toString();
   }
   
}
