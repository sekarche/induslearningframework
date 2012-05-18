package airldm2.classifiers.rl;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openrdf.model.URI;

import airldm2.classifiers.rl.estimator.AttributeValue;
import airldm2.classifiers.rl.estimator.Category;
import airldm2.util.CollectionUtil;

public class AggregatedInstance {

   private final URI mURI;

   private List<AttributeValue> mAttributeValue;
   
   private Category mTargetCategory;

   public AggregatedInstance(URI uri, Category targetCategory) {
      mURI = uri;
      mTargetCategory = targetCategory;
      mAttributeValue = CollectionUtil.makeList();
   }

   public List<AttributeValue> getAttributeValues() {
      return mAttributeValue;
   }
   
   public void addAttribute(AttributeValue count) {
      mAttributeValue.add(count);
   }
   
   public void removeLastAttribute() {
      mAttributeValue.remove(mAttributeValue.size() - 1);
   }

   public URI getURI() {
      return mURI;
   }
   
   public int getLabel() {
      return mTargetCategory.getIndex();
   }
   
   @Override
   public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
   }
   
}
