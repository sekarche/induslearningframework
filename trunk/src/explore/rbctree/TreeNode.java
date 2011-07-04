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

   private boolean isOpen;
   private double mScore;
   private int mDepth;
   private List<ValueIndexCount> mValueIndexCountForTuneInstances;
    
   public TreeNode() {
      mChildren = CollectionUtil.makeList();
      isOpen = true;
   }
   
   public TreeNode(RbcAttribute att, ClassValueCount rbcCount, List<ValueIndexCount> valueIndexCounts) {
      this();
      mAttribute = att;
      mRBCCount = rbcCount;
      mValueIndexCountForTuneInstances = valueIndexCounts;
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

   public void expand(List<RbcAttribute> childrenAtt, List<ClassValueCount> rbcCounts, List<List<ValueIndexCount>> valueIndexCountForAttributes) {
      for (int i = 0; i < childrenAtt.size(); i++) {
         RbcAttribute att = childrenAtt.get(i);
         ClassValueCount rbcCount = rbcCounts.get(i);
         List<ValueIndexCount> valueIndexCounts = valueIndexCountForAttributes.get(i);
         TreeNode child = new TreeNode(att, rbcCount, valueIndexCounts);
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

}
