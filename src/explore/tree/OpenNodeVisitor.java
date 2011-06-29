package explore.tree;


public interface OpenNodeVisitor extends TreeVisitor {
   
   TreeNode next();
   void clear();
   void reset();

}
