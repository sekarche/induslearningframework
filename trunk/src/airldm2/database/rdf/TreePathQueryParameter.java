package airldm2.database.rdf;


import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openrdf.model.URI;

import airldm2.classifiers.rl.tree.TreeEdge;
import airldm2.core.rl.RbcAttribute;
import airldm2.core.rl.RbcAttributeValue;

public class TreePathQueryParameter {
   
   public final URI TargetType;
   public final RbcAttribute Target;
   public final int TargetValueIndex;
   public List<RbcAttributeValue> AncestorAttValues;
   public List<TreeEdge> TreePath;
   public final RbcAttributeValue AttValue;

   public TreePathQueryParameter(URI targetType, RbcAttribute target, int targetValueIndex, RbcAttributeValue attValue) {
      TargetType = targetType;
      Target = target;
      TargetValueIndex = targetValueIndex;
      AttValue = attValue;
   }
   
   public TreePathQueryParameter(URI targetType, RbcAttribute target, int targetValueIndex, RbcAttribute att) {
      this(targetType, target, targetValueIndex, new RbcAttributeValue(att, null));
   }

   public TreePathQueryParameter(URI targetType, RbcAttribute target, int targetValueIndex, List<RbcAttributeValue> ancestorAttValues, List<TreeEdge> pathEdges, RbcAttributeValue attValue) {
      this(targetType, target, targetValueIndex, attValue);
      AncestorAttValues = ancestorAttValues;
      TreePath = pathEdges;
   }
      
   @Override
   public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
   }
   
}