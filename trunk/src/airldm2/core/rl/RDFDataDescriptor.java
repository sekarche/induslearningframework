package airldm2.core.rl;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

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

   public RDFDataDescriptor(URI targetType, String targetAttributeName) {
      this(targetType, targetAttributeName, CollectionUtil.<String,RbcAttribute>makeMap());
   }
   
   public RDFDataDescriptor(URI targetType, String targetAttributeName, Map<String,RbcAttribute> attributes) {
      mTargetType = targetType;
      mTargetAttributeName = targetAttributeName;
      mAttributes = attributes;
   }


   public void addAttribute(String attributeName, RbcAttribute attribute) {
      mAttributes.put(attributeName, attribute);
   }

   public Collection<RbcAttribute> getAttributes() {
      return  mAttributes.values();
   }

   public void setTargetAttribute(String attrib) {
      this.mTargetAttributeName = attrib;
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
      return null;
   }
   
   @Override
   public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
   }
   
}
