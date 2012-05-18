package explore.rbctree;

import java.util.List;

import airldm2.classifiers.rl.estimator.AttributeEstimator;
import airldm2.classifiers.rl.estimator.Histogram;
import airldm2.core.rl.RbcAttribute;

public class PropertyTree {

   private final OpenNodeVisitor mExpansionStrategy;
   private TreeNode mRoot;

   public PropertyTree() {
      mExpansionStrategy = new BestScore();
      mExpansionStrategy.reset();
      mRoot = new TreeNode();
   }

   public TreeNode getNextNodeToExpand() {
      mExpansionStrategy.clear();
      mRoot.accept(mExpansionStrategy);
      return mExpansionStrategy.next();
   }

   public int attributeSize() {
      AttributeCounter v = new AttributeCounter();
      mRoot.accept(v);
      return v.getCount();
   }

   public void expand(TreeNode n, List<RbcAttribute> childrenAtt, List<AttributeEstimator> rbcCounts, List<AttributeEstimator> rbcCounts2, List<List<Histogram>> valueIndexCountForAttributes, List<List<Histogram>> valueIndexCountForAttributes2) {
      n.expand(childrenAtt, rbcCounts, rbcCounts2, valueIndexCountForAttributes, valueIndexCountForAttributes2);
   }
   
   public void print() {
      Printer v = new Printer();
      mRoot.accept(v);
   }

   public void accept(TreeVisitor treeVisitor) {
      mRoot.accept(treeVisitor);
   }

}
