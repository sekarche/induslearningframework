package airldm2.classifiers.rl.tree;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import airldm2.classifiers.rl.estimator.Category;
import airldm2.classifiers.rl.estimator.Histogram;
import airldm2.core.ISufficentStatistic;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
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
   private final RbcAttribute mAtt;
   
   public TreeNodeSplitter(RDFDataSource dataSource, RDFDataDescriptor dataDesc, RbcAttribute att) {
      mSource = dataSource;
      mDesc = dataDesc;
      mAtt = att;
   }
   
   public RbcAttribute getAttribute() {
      return mAtt;
   }
   
   public void estimateParameters(List<TreeNodeSplitter> ancestors, List<Category> path) throws RDFDatabaseException {
      RbcAttribute targetAttribute = mDesc.getTargetAttribute();
      int numOfClassLabels = targetAttribute.getDomainSize();
      int numOfAttributeValues = mAtt.getDomainSize();

      List<RbcAttribute> ancestorAtts = getAttributeList(ancestors);
      
      mValueHistograms = new Histogram[numOfClassLabels];
      for (int j = 0; j < numOfClassLabels; j++) {
         double[] valueCounts = new double[numOfAttributeValues];
         for (int k = 0; k < numOfAttributeValues; k++) {
            TreePathQueryParameter queryParam = new TreePathQueryParameter(mDesc.getTargetType(), targetAttribute, j, ancestorAtts, path, mAtt, k);
            ISufficentStatistic tempSuffStat = mSource.getTreePathSufficientStatistic(queryParam);
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
   
   public List<RbcAttribute> getAttributeList(List<TreeNodeSplitter> as) {
      List<RbcAttribute> list = CollectionUtil.makeList();
      for (TreeNodeSplitter a : as) {
         list.add(a.getAttribute());
      }
      return list;
   }

   public double getInfoGain() {
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
   
   public double getTruePositiveCount(Category v) {
      int i = v.getIndex();
      int prediction = getPredictionAtLeaf(v);
      return mValueHistograms[prediction].get(i);
   }
   
   public int getPredictionAtLeaf(Category v) {
      int i = v.getIndex();
      double[] vs = new double[mClassHistogram.size()];
      for (int j = 0; j < vs.length; j++) {
         vs[j] = mValueHistograms[j].get(i);
      }
      return MathUtil.maxIndex(vs);
   }

   @Override
   public String toString() {
      return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
         .append("Attribute", mAtt.getName())
         .append("Hierarchy", mAtt.getExtendedHierarchy())
         .append("mValueHistograms", mValueHistograms)
         .toString();
   }
   
}
