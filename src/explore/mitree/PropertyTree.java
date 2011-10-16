package explore.mitree;

import java.util.List;

import airldm2.core.rl.PropertyChain;
import airldm2.core.rl.RbcAttribute;

public class PropertyTree {

   private final OpenNodeVisitor mExpansionStrategy;
   private TreeNode mRoot;

   public PropertyTree(OpenNodeVisitor expansionStrategy) {
      mExpansionStrategy = expansionStrategy;
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

   public List<RbcAttribute> getAllAttributes() {
      AttributeCollector v = new AttributeCollector();
      mRoot.accept(v);
      return v.getAttributes();
   }

   public List<RbcAttributeScore> getAllRbcAttributeScores() {
      AttributeScoreCollector v = new AttributeScoreCollector();
      mRoot.accept(v);
      return v.getAttributes();
   }
   
   public void expand(TreeNode n, RbcAttributeScore attribute, List<PropertyChain> childrenProp) {
      n.expand(attribute, childrenProp);
   }

   public void print() {
      Printer v = new Printer();
      mRoot.accept(v);
   }
   
}
