package airldm2.core.rl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openrdf.model.URI;

import airldm2.core.DataDescriptor;
import airldm2.util.CollectionUtil;

/**
 * TODO Write class description here.
 *
 * @author neeraj (TODO Write email id here)
 * @since Jan 24, 2011
 * @version $Date: $
 */
public class RDFDataDescriptor implements DataDescriptor {

   private URI mTargetType;
   private String mTargetAttributeName;

   /**
    * A HashMap of attributes indexed by attribute name
    */
   private Map<String,RbcAttribute> mAttributes;
   
   private List<RbcAttribute> cNonTargetAttributeList;

   public RDFDataDescriptor(URI targetType, String targetAttributeName) {
      this(targetType, targetAttributeName, CollectionUtil.<String,RbcAttribute>makeMap());
   }
   
   public RDFDataDescriptor(URI targetType, String targetAttributeName, Map<String,RbcAttribute> attributes) {
      mTargetType = targetType;
      mTargetAttributeName = targetAttributeName;
      mAttributes = attributes;
      prepareList();
   }

   private void prepareList() {
      cNonTargetAttributeList = CollectionUtil.makeList();
      for (Entry<String, RbcAttribute> entry : mAttributes.entrySet()) {
         if (mTargetAttributeName.equals(entry.getKey())) continue;
         getAttributeList().add(entry.getValue());
      }
   }

   public URI getTargetType() {
      return mTargetType;
   }
   
   public List<RbcAttribute> getAttributeList() {
      return cNonTargetAttributeList;
   }

   public RbcAttribute getTargetAttribute() {
      return mAttributes.get(mTargetAttributeName);
   }
   
   @Override
   public String getDataName() {
      return null;
   }

   @Override
   public String getProperty(String key) {
      return null;
   }

   @Override
   public int getAttributeCount() {
      return mAttributes.size();
   }

   @Override
   public String[] getClassLabels() {
      List<String> values = getTargetAttribute().getValueType().getStringValues();
      String[] labels = new String[values.size()];
      values.toArray(labels);
      return labels;
   }
   
   @Override
   public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
   }

}
