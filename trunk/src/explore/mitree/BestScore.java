package explore.mitree;

import java.util.List;

import airldm2.util.CollectionUtil;

public class BestScore implements OpenNodeVisitor {

   private List<TreeNode> mList;
   
   public BestScore() {
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
      
      TreeNode max = mList.get(0);
      for (TreeNode n : mList) {
         if (n.getScore() > max.getScore()) {
            max = n;
         }
      }
      return max;
   }

   @Override
   public void clear() {
      mList.clear();
   }
   
   @Override
   public void reset() {
   }
   
}
