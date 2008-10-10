package airldm2.exceptions;


public class NotImplementedException extends Exception {
   
   int code;

   public NotImplementedException(int code, String message) {
  super(message);
  this.code = code;
   }

   public int getCode() {
  return code;
   }

}
