package airldm2.classifiers.rl;

import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openrdf.model.URI;

import airldm2.classifiers.rl.estimator.AttributeValue;
import airldm2.classifiers.rl.estimator.Category;
import airldm2.core.rl.RbcAttribute;
import airldm2.util.CollectionUtil;

public class AggregatedInstance {

   private final URI mURI;

   private Map<RbcAttribute, AttributeValue> mAttributeValue;
   
   private Category mTargetCategory;
   
   private RbcAttribute cLastAddedAttribute;

   public AggregatedInstance(URI uri, Category targetCategory) {
      mURI = uri;
      mTargetCategory = targetCategory;
      mAttributeValue = CollectionUtil.makeMap();
   }

   public Map<RbcAttribute, AttributeValue> getAttributeValues() {
      return mAttributeValue;
   }
   
   public void addAttribute(RbcAttribute att, AttributeValue value) {
      mAttributeValue.put(att, value);
      cLastAddedAttribute = att;
   }
   
   public void removeLastAttribute() {
      if (cLastAddedAttribute != null) {
         mAttributeValue.remove(cLastAddedAttribute);
      }
   }

   public URI getURI() {
      return mURI;
   }
   
   public int getLabel() {
      return mTargetCategory.getIndex();
   }
   
   @Override
   public String toString() {
      return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
      .append("mTargetCategory", mTargetCategory)
      .append("mAttributeValue", mAttributeValue.values())
      .toString();
   }
   
}
