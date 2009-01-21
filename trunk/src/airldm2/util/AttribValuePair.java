/**
 * A Utility Class to Set Name Value Pairs
 */
package airldm2.util;

/**
 * @author neeraj
 * 
 */
public class AttribValuePair {
   private String name;

   private String value;

   private boolean includeMissingValue = false;

   private String missingValue = null;

   /**
    * @param name the name to set
    */
   public void setAttribName(String name) {
      this.name = name;
   }

   /**
    * @return the name
    */
   public String getAttribName() {
      return name;
   }

   /**
    * @param value the value to set
    */
   public void setAttribValue(String value) {
      this.value = value;
   }

   /**
    * @return the value
    */
   public String getAttribValue() {
      return value;
   }

   public AttribValuePair(String name, String value) {
      this.name = name;
      this.value = value;
   }

   /**
    * This function indicates that a missing Value representation is to be
    * included as possible value for this attribute. The passed
    * missingValue is a representation of missing value (e.g "?"); It
    * should not be passed null ( string "null" is okay)
    * 
    * @param missingValue
    */
   public void setIncludeMissingValue(String missingValue) {

      // do nothing if null is passed
      if (missingValue == null)
         return;

      includeMissingValue = true;
      this.missingValue = missingValue;
   }

   /**
    * If missing value represntation should be counted as a possible value
    * for this attribute
    * 
    * @return
    */
   public boolean IsIncludeMissingValue() {
      return includeMissingValue;
   }

   /**
    * The missing value representation of this attribute. Should be only
    * used if function IsIncludeMissingValue() returns true
    * 
    * @return
    */
   public String getMissingValueRepresentation() {
      return this.missingValue;
   }

   public AttribValuePair() {
   }

}
