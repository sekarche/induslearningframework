package explore.tree;

import java.util.List;

import airldm2.core.rl.PropertyChain;
import airldm2.util.CollectionUtil;
import explore.RbcAttributeScore;

public class TreeNode {

   private TreeNode mParent;
   private List<TreeNode> mChildren;
   private PropertyChain mPropertyChain;
   private boolean isOpen;
   private RbcAttributeScore mAttributeScore;
   private int mDepth;
    
   public TreeNode() {
      mChildren = CollectionUtil.makeList();
      isOpen = true;
   }
   
   public TreeNode(PropertyChain prop) {
      this();
      mPropertyChain = prop;
   }

   public void accept(TreeVisitor v) {
      v.visit(this);
      for (TreeNode n : mChildren) {
         n.accept(v);
      }
   }

   public TreeNode getParent() {
      return mParent;
   }
   
   public PropertyChain getPropertyChain() {
      return mPropertyChain;
   }
   
   public RbcAttributeScore getAttributeScore() {
      return mAttributeScore;
   }

   public void expand(RbcAttributeScore att, List<PropertyChain> childrenProp) {
      mAttributeScore = att;
      
      for (PropertyChain p : childrenProp) {
         TreeNode child = new TreeNode(p);
         child.mParent = this;
         child.mDepth = mDepth + 1;
         mChildren.add(child);         
      }
      
      isOpen = false;
   }

   private double getChildrenAverageAttributeScore() {
      if (!hasChildWithAttribute()) return 0.0;
      
      double sum = 0.0;
      for (TreeNode n : mChildren) {
         if (n.mAttributeScore != null) {
            sum += n.mAttributeScore.Score;
         }
      }
      
      return sum / mChildren.size();
   }

   public boolean isOpen() {
      return isOpen;
   }
   
   public int getDepth() {
      return mDepth;
   }
     
   private boolean hasChildWithAttribute() {
      for (TreeNode n : mChildren) {
         if (n.mAttributeScore != null) return true;
      }
      return false;
   }
   
   private RbcAttributeScore getMostRecentAncestorAttributeScore() {
      if (mParent == null) return null;
      if (mParent.mAttributeScore == null) {
         return mParent.getMostRecentAncestorAttributeScore();
      } else {
         return mParent.mAttributeScore;
      }
   }
   
   public double getScore() {
      if (mParent == null) {
         return 1.0;
      } else {
         if (mParent.hasChildWithAttribute()) {
            return mParent.getChildrenAverageAttributeScore();
         } else {
            RbcAttributeScore anc = getMostRecentAncestorAttributeScore();
            if (anc == null) {
               return 0.0;
            } else {
               return anc.Score;
            }
         }
      }
   }
   
}
