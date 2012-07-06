package airldm2.database.rdf;


import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openrdf.model.URI;

import airldm2.core.rl.RbcAttribute;
import airldm2.core.rl.RbcAttributeValue;

public class TreePathQueryParameter {
   
   public final URI TargetType;
   public final RbcAttribute Target;
   public final int TargetValueIndex;
   public final List<RbcAttributeValue> AncestorAttValues;
   public final List<Boolean> TreePath;
   public final RbcAttributeValue AttValue;

   
   public TreePathQueryParameter(URI targetType, RbcAttribute target, int targetValueIndex, List<RbcAttributeValue> ancestorAttValues, List<Boolean> pathEdges, RbcAttributeValue mAttValue) {
      TargetType = targetType;
      Target = target;
      TargetValueIndex = targetValueIndex;
      AncestorAttValues = ancestorAttValues;
      TreePath = pathEdges;
      AttValue = mAttValue;
   }
      
   @Override
   public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
   }
   
}