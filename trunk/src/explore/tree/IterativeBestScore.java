package explore.tree;

import java.util.List;

import airldm2.util.CollectionUtil;

public class IterativeBestScore implements OpenNodeVisitor {

   private List<TreeNode> mList;
   private int mCurrentDepth;
   private double mCurrentMaxDepth;
   
   public IterativeBestScore() {
      mList = CollectionUtil.makeList();
      mCurrentDepth = 0;
      mCurrentMaxDepth = 2;
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
            
      TreeNode max = maxBelowDepth(mCurrentDepth);
      incrementDepth();
      
      while (max == null) {
         max = maxBelowDepth(mCurrentDepth);
         incrementDepth();
      }

      return max;
   }

   private void incrementDepth() {
      mCurrentDepth++;
      if (mCurrentDepth > mCurrentMaxDepth) {
         mCurrentDepth = 1;
         mCurrentMaxDepth += 1.0;
      }
   }

   private TreeNode maxBelowDepth(int depth) {
      TreeNode max = null;
      for (TreeNode n : mList) {
         if (n.getDepth() > depth) continue;
         
         if (max == null) {
            max = n;
         } else {
            if (n.getScore() > max.getScore()) {
               max = n;
            }
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
      mCurrentDepth = 0;
      mCurrentMaxDepth = 2;
   }
   
}
