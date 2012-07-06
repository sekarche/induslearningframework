package airldm2.core.rl;

import java.util.List;

import com.clarkparsia.pellint.util.CollectionUtil;

public class RbcAttributeValue {

   public RbcAttribute Attribute;
   public String ValueKey;
   
   public RbcAttributeValue(RbcAttribute att, String key) {
      Attribute = att;
      ValueKey = key;
   }

   public static List<RbcAttributeValue> makeAllValues(RbcAttribute att) {
      List<String> domain = att.getDomain();
      List<RbcAttributeValue> values = CollectionUtil.makeList();
      for (String key : domain) {
         values.add(new RbcAttributeValue(att, key));
      }
      return values;
   }
   
   @Override
   public int hashCode() {
      return Attribute.hashCode() + ValueKey.hashCode();
   }
   
   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof RbcAttributeValue)) return false;
      RbcAttributeValue other = (RbcAttributeValue) obj;
      return Attribute.equals(other.Attribute) && ValueKey.equals(other.ValueKey);
   }
   
}
