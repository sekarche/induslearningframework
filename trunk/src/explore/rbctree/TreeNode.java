package explore.rbctree;

import java.util.List;

import airldm2.classifiers.rl.estimator.AttributeEstimator;
import airldm2.classifiers.rl.estimator.Histogram;
import airldm2.core.rl.PropertyChain;
import airldm2.core.rl.RbcAttribute;
import airldm2.util.CollectionUtil;

public class TreeNode {

   private TreeNode mParent;
   private List<TreeNode> mChildren;
   
   private RbcAttribute mAttribute;
   private AttributeEstimator mRBCCount;
   private AttributeEstimator mRBCCount2;

   private boolean isOpen;
   private double mScore;
   private int mDepth;
   private List<Histogram> mValueIndexCountForTuneInstances;
   private List<Histogram> mValueIndexCountForTuneInstances2;
    
   public TreeNode() {
      mChildren = CollectionUtil.makeList();
      isOpen = true;
   }
   
   public TreeNode(RbcAttribute att, AttributeEstimator rbcCount, AttributeEstimator rbcCount2, List<Histogram> valueIndexCounts, List<Histogram> valueIndexCounts2) {
      this();
      mAttribute = att;
      mRBCCount = rbcCount;
      mRBCCount2 = rbcCount2;
      mValueIndexCountForTuneInstances = valueIndexCounts;
      mValueIndexCountForTuneInstances2 = valueIndexCounts2;
   }

   public void accept(TreeVisitor v) {
      v.visit(this);
      for (TreeNode n : mChildren) {
         n.accept(v);
      }
   }

   public RbcAttribute getAttribute() {
      return mAttribute;
   }

   public PropertyChain getPropertyChain() {
      if (mAttribute == null) return null;
      return mAttribute.getPropertyChain();
   }

   public void expand(List<RbcAttribute> childrenAtt, List<AttributeEstimator> rbcCounts, List<AttributeEstimator> rbcCounts2, List<List<Histogram>> valueIndexCountForAttributes, List<List<Histogram>> valueIndexCountForAttributes2) {
      for (int i = 0; i < childrenAtt.size(); i++) {
         RbcAttribute att = childrenAtt.get(i);
         AttributeEstimator rbcCount = rbcCounts.get(i);
         List<Histogram> valueIndexCounts = valueIndexCountForAttributes.get(i);
         AttributeEstimator rbcCount2 = rbcCounts2.get(i);
         List<Histogram> valueIndexCounts2 = valueIndexCountForAttributes2.get(i);
         TreeNode child = new TreeNode(att, rbcCount, rbcCount2, valueIndexCounts, valueIndexCounts2);
         child.mParent = this;
         child.mDepth = mDepth + 1;
         mChildren.add(child);         
      }
      
      isOpen = false;
      
      if (mParent != null) {
         for (TreeNode sib : mParent.mChildren) {
            if (mAttribute.getPropertyChain().equals(sib.mAttribute.getPropertyChain())) {
               sib.isOpen = false;
            }
         }
      }
   }

   public boolean isOpen() {
      return isOpen;
   }
   
   public int getDepth() {
      return mDepth;
   }
        
   public double getScore() {
      return mScore;
   }
   
   public void setScore(double v) {
      mScore = v;
   }

   public AttributeEstimator getRBCCount() {
      return mRBCCount;
   }
   
   public List<Histogram> getValueIndexCountForTuneInstances() {
      return mValueIndexCountForTuneInstances;
   }

   public AttributeEstimator getRBCCount2() {
      return mRBCCount2;
   }
   
   public List<Histogram> getValueIndexCountForTuneInstances2() {
      return mValueIndexCountForTuneInstances2;
   }

}
