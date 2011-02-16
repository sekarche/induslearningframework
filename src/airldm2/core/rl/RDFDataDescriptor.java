/**
 * RDFDataDescriptor.java<br>
 * TODO Write description for RDFDataDescriptor.java.
 *
 * $Header: $
 */

package airldm2.core.rl;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

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
   private Map<String,URI> mPrefixes;

   /**
    * A HashMap of attributes indexed  by attribute name
    */
   private Map<String,RbcAttribute> mAttributes;

   public RDFDataDescriptor(URI targetType, String targetAttributeName) {
      this(targetType, targetAttributeName, CollectionUtil.<String,RbcAttribute>makeMap());
   }
   
   public RDFDataDescriptor(URI targetType, String targetAttributeName, Map<String,RbcAttribute> attributes) {
      mTargetType = targetType;
      mTargetAttributeName = targetAttributeName;
      mAttributes = attributes;
      
      mPrefixes = CollectionUtil.makeMap();
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
   
   public void addPrefix(String prefix, URI resolved) {
      mPrefixes.put(prefix, resolved);
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
   
}
