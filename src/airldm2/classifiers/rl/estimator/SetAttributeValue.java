package airldm2.classifiers.rl.estimator;

import java.util.Map;

import org.openrdf.model.URI;

import airldm2.util.CollectionUtil;


public class SetAttributeValue implements AttributeValue {

   private Map<URI,AttributeValue> mValues;

   public SetAttributeValue() {
      mValues = CollectionUtil.makeMap();
   }
   
   public void add(URI c, AttributeValue value) {
      mValues.put(c, value);
   }

   public AttributeValue get(URI c) {
      return mValues.get(c);
   }

   @Override
   public String toString() {
      return mValues.toString();
   }
   
}
