package explore.rbctree;



public class AttributeCounter implements TreeVisitor {

   private int mCount;
   
   public AttributeCounter() {
      mCount = 0;
   }
   
   @Override
   public void visit(TreeNode node) {
      if (node.getAttribute() != null && !node.isOpen()) {
         mCount++;
      }
   }

   public int getCount() {
      return mCount;
   }

}
