/**
 * 
 */
package airldm2.exceptions;

/**
 * @author neeraj Define Some RunTime Exceptions for Incorrect
 * Usage/Configuration
 */
public class RTConfigException extends Exception {
   /** The Constant serialVersionUID. */
   private static final long serialVersionUID = 2L;

   // TODO: A larger implemntation on lines of DatabaseExceptions

   int code = -1;

   public RTConfigException(int code, String message) {
      super(message);
      this.code = code;
   }

   public RTConfigException(String msg, Exception e) {
      // super(e);
      super(msg, e);
   }

   public RTConfigException(int code, String msg, Exception e) {
      // super(e);
      super(msg, e);
      this.code = code;
   }

   public int getCode() {
      return code;
   }

}
