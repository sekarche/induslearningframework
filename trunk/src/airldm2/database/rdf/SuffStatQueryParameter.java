package airldm2.database.rdf;

import java.net.URI;

import airldm2.core.rl.RbcAttribute;

public class SuffStatQueryParameter {
   
   public URI TargetType;

   public RbcAttribute Target;
   public int TargetValueIndex;

   public RbcAttribute Feature;
   public int FeatureValueIndex;
   
   public SuffStatQueryParameter(URI targetType, RbcAttribute target, int targetValueIndex) {
      this(targetType, target, targetValueIndex, null, 0);
   }
   
   public SuffStatQueryParameter(URI targetType, RbcAttribute target, int targetValueIndex, RbcAttribute feature, int featureValueIndex) {
      TargetType = targetType;
      Target = target;
      TargetValueIndex = targetValueIndex;
      Feature = feature;
      FeatureValueIndex = featureValueIndex;
   }
   
   public boolean hasFeature() {
      return Feature != null;
   }
   
}