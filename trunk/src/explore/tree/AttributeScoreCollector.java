package explore.tree;

import java.util.List;

import airldm2.util.CollectionUtil;
import explore.RbcAttributeScore;

public class AttributeScoreCollector implements TreeVisitor {

   private List<RbcAttributeScore> mAttributes;
   
   public AttributeScoreCollector() {
      mAttributes = CollectionUtil.makeList();
   }
   
   @Override
   public void visit(TreeNode node) {
      RbcAttributeScore s = node.getAttributeScore();
      if (s == null) return;
      
      mAttributes.add(s);
   }

   public List<RbcAttributeScore> getAttributes() {
      return mAttributes;
   }

}
