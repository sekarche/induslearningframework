package explore.tree;



public class Printer implements TreeVisitor {

   @Override
   public void visit(TreeNode node) {
      System.out.println(node.getScore() + ": " + node.getPropertyChain());
   }

}
