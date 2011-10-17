package airldm2.classifiers.rl;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openrdf.model.URI;

import airldm2.util.CollectionUtil;

public class AggregatedInstance {

   private final URI mURI;

   /* mAttributeValueIndexOrCount[attribute index][value index] = count of value occurrences
    * If mAggregatorType != INDEPENDENT_VAL, then the sum of counts is exactly 1
    */
   private List<ValueIndexCount> mAttributeValueIndexCount;
   
   private ValueIndexCount mTargetValueIndexCount;

   public AggregatedInstance(URI uri, ValueIndexCount targetValueIndexCount) {
      mURI = uri;
      mTargetValueIndexCount = targetValueIndexCount;
      mAttributeValueIndexCount = CollectionUtil.makeList();
   }

   public List<ValueIndexCount> getAttributeValueIndexCount() {
      return mAttributeValueIndexCount;
   }
   
   public void addAttribute(ValueIndexCount count) {
      mAttributeValueIndexCount.add(count);
   }
   
   public void removeLastAttribute() {
      mAttributeValueIndexCount.remove(mAttributeValueIndexCount.size() - 1);
   }

   public URI getURI() {
      return mURI;
   }
   
   public int getLabel() {
      for (int i = 0; i < mTargetValueIndexCount.size(); i++) {
         if (mTargetValueIndexCount.get(i) > 0) {
            return i;
         }
      }
      
      return -1;
   }
   
   @Override
   public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
   }
   
}
