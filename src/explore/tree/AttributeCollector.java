package explore.tree;

import java.util.List;

import airldm2.core.rl.RbcAttribute;
import airldm2.util.CollectionUtil;
import explore.RbcAttributeScore;

public class AttributeCollector implements TreeVisitor {

   private List<RbcAttribute> mAttributes;
   
   public AttributeCollector() {
      mAttributes = CollectionUtil.makeList();
   }
   
   @Override
   public void visit(TreeNode node) {
      RbcAttributeScore s = node.getAttributeScore();
      if (s == null) return;
      
      mAttributes.add(s.Attribute);
   }

   public List<RbcAttribute> getAttributes() {
      return mAttributes;
   }

}
