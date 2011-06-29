package explore.tree;

import explore.RbcAttributeScore;


public class AttributeCounter implements TreeVisitor {

   private int mCount;
   
   public AttributeCounter() {
      mCount = 0;
   }
   
   @Override
   public void visit(TreeNode node) {
      RbcAttributeScore s = node.getAttributeScore();
      if (s == null) return;
      
      mCount++;
   }

   public int getCount() {
      return mCount;
   }

}
