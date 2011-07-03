package explore.rbctree;

import java.util.List;

import airldm2.core.rl.PropertyChain;
import airldm2.core.rl.RbcAttribute;
import airldm2.util.CollectionUtil;

public class TreeNode {

   private TreeNode mParent;
   private List<TreeNode> mChildren;
   private RbcAttribute mAttribute;
   private boolean isOpen;
   private double mScore;
   private int mDepth;
    
   public TreeNode() {
      mChildren = CollectionUtil.makeList();
      isOpen = true;
   }
   
   public TreeNode(RbcAttribute att) {
      this();
      mAttribute = att;
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

   public void expand(List<RbcAttribute> childrenAtt) {
      for (RbcAttribute att : childrenAtt) {
         TreeNode child = new TreeNode(att);
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

}
