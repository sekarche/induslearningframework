package airldm2.database.rdf;


import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openrdf.model.URI;

import airldm2.classifiers.rl.estimator.Category;
import airldm2.core.rl.RbcAttribute;

public class TreePathQueryParameter {
   
   public final URI TargetType;
   public final RbcAttribute Target;
   public final int TargetValueIndex;
   public final List<RbcAttribute> AncestorAtts;
   public final List<Category> TreePath;
   public final RbcAttribute Feature;
   public final int FeatureValueIndex;

   
   public TreePathQueryParameter(URI targetType, RbcAttribute target, int targetValueIndex, List<RbcAttribute> ancestorAtts, List<Category> path, RbcAttribute feature, int featureValueIndex) {
      TargetType = targetType;
      Target = target;
      TargetValueIndex = targetValueIndex;
      AncestorAtts = ancestorAtts;
      TreePath = path;
      Feature = feature;
      FeatureValueIndex = featureValueIndex;
   }
      
   @Override
   public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
   }
   
}