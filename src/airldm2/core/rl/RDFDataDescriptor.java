package airldm2.core.rl;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
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

   private String mInstanceVar;
   private String mValueVar;
   private String mHierarchyVar;
   
   private URI mTargetType;
   private String mTargetAttributeName;

   /**
    * A HashMap of attributes indexed by attribute name
    */
   private Map<String,RbcAttribute> mAttributes;
   
   private List<RbcAttribute> cNonTargetAttributeList;

   public RDFDataDescriptor(String instanceVar, String valueVar, String hierarchyVar, URI targetType, String targetAttributeName) {
      this(instanceVar, valueVar, hierarchyVar, targetType, targetAttributeName, CollectionUtil.<String,RbcAttribute>makeMap());
   }
   
   public RDFDataDescriptor(String instanceVar, String valueVar, String hierarchyVar, URI targetType, String targetAttributeName, Map<String,RbcAttribute> attributes) {
      mInstanceVar = instanceVar;
      mValueVar = valueVar;
      mHierarchyVar = hierarchyVar;
      mTargetType = targetType;
      mTargetAttributeName = targetAttributeName;
      mAttributes = attributes;
      prepareList();
   }

   private void prepareList() {
      cNonTargetAttributeList = CollectionUtil.makeList();
      for (Entry<String, RbcAttribute> entry : mAttributes.entrySet()) {
         if (mTargetAttributeName.equals(entry.getKey())) continue;
         getNonTargetAttributeList().add(entry.getValue());
      }
   }

   public String getInstanceVar() {
      return mInstanceVar;
   }
   
   public String getValueVar() {
      return mValueVar;
   }
   
   public String getHierarchyVar() {
      return mHierarchyVar;
   }
   
   public URI getTargetType() {
      return mTargetType;
   }
   
   public List<RbcAttribute> getNonTargetAttributeList() {
      return cNonTargetAttributeList;
   }
   
   public int getNonTargetAttributeCount() {
      return cNonTargetAttributeList.size();
   }

   public void clearNonTargetAttributes() {
      RbcAttribute targetAttribute = mAttributes.get(mTargetAttributeName);
      
      mAttributes.clear();
      cNonTargetAttributeList.clear();
      mAttributes.put(mTargetAttributeName, targetAttribute);
   }
   
   public void addNonTargetAttributes(List<RbcAttribute> as) {
      for (RbcAttribute a : as) {
         addNonTargetAttribute(a);
      }
   }
   
   public void addNonTargetAttribute(RbcAttribute a) {
      mAttributes.put(a.getName(), a);
      cNonTargetAttributeList.add(a);
   }
   
   public RbcAttribute getTargetAttribute() {
      return mAttributes.get(mTargetAttributeName);
   }
   
   public Collection<RbcAttribute> getAllAttributes() {
      return mAttributes.values();
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

   public void write(Writer out) throws IOException {
      out.write(RDFDataDescriptorParser.TARGET_TYPE);
      out.write(mTargetType.stringValue());
      out.write("\n\n");
      
      out.write(RDFDataDescriptorParser.TARGET);
      out.write(mTargetAttributeName);
      out.write("\n\n");
      
      for (RbcAttribute a : mAttributes.values()) {
         a.write(out);
         out.write("\n");
      }
   }

   public RDFDataDescriptor copy() {
      RDFDataDescriptor copy = new RDFDataDescriptor(mInstanceVar, mValueVar, mHierarchyVar, mTargetType, mTargetAttributeName, CollectionUtil.makeMap(mAttributes));
      return copy;
   }

}
