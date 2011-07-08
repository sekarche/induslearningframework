package explore.rbctree;

import java.util.List;

import airldm2.classifiers.rl.ClassValueCount;
import airldm2.classifiers.rl.ValueIndexCount;
import airldm2.core.rl.PropertyChain;
import airldm2.core.rl.RbcAttribute;
import airldm2.util.CollectionUtil;

public class TreeNode {

   private TreeNode mParent;
   private List<TreeNode> mChildren;
   
   private RbcAttribute mAttribute;
   private ClassValueCount mRBCCount;
   private ClassValueCount mRBCCount2;

   private boolean isOpen;
   private double mScore;
   private int mDepth;
   private List<ValueIndexCount> mValueIndexCountForTuneInstances;
   private List<ValueIndexCount> mValueIndexCountForTuneInstances2;
    
   public TreeNode() {
      mChildren = CollectionUtil.makeList();
      isOpen = true;
   }
   
   public TreeNode(RbcAttribute att, ClassValueCount rbcCount, ClassValueCount rbcCount2, List<ValueIndexCount> valueIndexCounts, List<ValueIndexCount> valueIndexCounts2) {
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

   public void expand(List<RbcAttribute> childrenAtt, List<ClassValueCount> rbcCounts, List<ClassValueCount> rbcCounts2, List<List<ValueIndexCount>> valueIndexCountForAttributes, List<List<ValueIndexCount>> valueIndexCountForAttributes2) {
      for (int i = 0; i < childrenAtt.size(); i++) {
         RbcAttribute att = childrenAtt.get(i);
         ClassValueCount rbcCount = rbcCounts.get(i);
         List<ValueIndexCount> valueIndexCounts = valueIndexCountForAttributes.get(i);
         ClassValueCount rbcCount2 = rbcCounts2.get(i);
         List<ValueIndexCount> valueIndexCounts2 = valueIndexCountForAttributes2.get(i);
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

   public ClassValueCount getRBCCount() {
      return mRBCCount;
   }
   
   public List<ValueIndexCount> getValueIndexCountForTuneInstances() {
      return mValueIndexCountForTuneInstances;
   }

   public ClassValueCount getRBCCount2() {
      return mRBCCount2;
   }
   
   public List<ValueIndexCount> getValueIndexCountForTuneInstances2() {
      return mValueIndexCountForTuneInstances2;
   }

}
