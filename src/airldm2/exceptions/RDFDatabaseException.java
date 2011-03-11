package airldm2.exceptions;


public class RDFDatabaseException extends Exception {

   private static final long serialVersionUID = 1L;

   public RDFDatabaseException(String s) {
      super(s);
   }
   
   public RDFDatabaseException(String s, Throwable e) {
      super(s, e);
   }

   public RDFDatabaseException(Throwable e) {
      super(e);
   }
   
}
