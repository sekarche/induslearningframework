package explore.mitree;

import java.util.List;

import airldm2.util.CollectionUtil;

public class BFS implements OpenNodeVisitor {

   private List<TreeNode> mList;
   
   public BFS() {
      mList = CollectionUtil.makeList();
   }
   
   @Override
   public void visit(TreeNode node) {
      if (node.isOpen()) {
         mList.add(node);
      }
   }

   @Override
   public TreeNode next() {
      if (mList.isEmpty()) return null;
      
      TreeNode min = mList.get(0);
      for (TreeNode n : mList) {
         if (n.getDepth() < min.getDepth()) {
            min = n;
         }
      }
      return min;
   }

   @Override
   public void clear() {
      mList.clear();
   }
   
   @Override
   public void reset() {
   }
   
}
