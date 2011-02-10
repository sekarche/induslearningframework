/**
 * RDFDataDescriptor.java<br>
 * TODO Write description for RDFDataDescriptor.java.
 *
 * $Header: $
 */

package airldm2.core.datatypes.relational;

import airldm2.core.DataDescriptor;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import airldm2.core.rl.RbcAttribute;

/**
 * TODO Write class description here.
 *
 * @author neeraj (TODO Write email id here)
 * @since Jan 24, 2011
 * @version $Date: $
 */
public class RDFDataDescriptor implements DataDescriptor {
   
  String RDFdataSourceName ="";
  private  URI core_item;
  int attribute_count =0;
  String targetAttributeName;
  
  /**
   * A HashMap of attributes indexed  by attribute name
   */
  private  HashMap<String,RbcAttribute> attributes = new HashMap<String,RbcAttribute>();
  
  /**
   * A HashMap to resolve prefixed
   */
  private HashMap <String,URI> prefixes = new HashMap<String,URI>();
  
  public void addAttribute(String attributeName, RbcAttribute attribute ) {
     attributes.put(attributeName, attribute);
     attribute_count++;
  }
  
  
  public Collection<RbcAttribute> getAttributes() {
    return  attributes.values();
  }
   
  public void setTargetAttribute(String attrib) {
     this.targetAttributeName = attrib;
  }
  
  public RbcAttribute getTargetAttribute() {
     return  attributes.get(targetAttributeName);
  }
 public void setRDFdataSourceName(String name) {
     this.RDFdataSourceName = name;
 }
 public void addPrefix(String prefix,  URI resolved) {
    prefixes.put(prefix, resolved);
 }

   /* (non-Javadoc)
    * @see airldm2.core.DataDescriptor#getDataName()
    */
   @Override
   public String getDataName() {
      return RDFdataSourceName;
   }

   /* (non-Javadoc)
    * @see airldm2.core.DataDescriptor#getProperty(java.lang.String)
    */
   @Override
   public String getProperty(String key) {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see airldm2.core.DataDescriptor#getAttributeCount()
    */
   @Override
   public int getAttributeCount() {
     
      return  attribute_count;
   }

   /* (non-Javadoc)
    * @see airldm2.core.DataDescriptor#getClassLabels()
    */
   @Override
   public String[] getClassLabels() {
      // TODO Auto-generated method stub
      return null;
   }
   

}
