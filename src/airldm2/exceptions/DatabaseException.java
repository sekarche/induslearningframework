/**
 * DatabaseException.java<br>
 * Exception specialization for handling database exceptions.
 *
 * $Header: /home/CVS/airldm2/src/airldm2/exceptions/DatabaseException.java,v 1.1 2008/02/03 18:33:43 neeraj Exp $
 */
package airldm2.exceptions;

import java.sql.SQLException;

/**
 * Defines the DatabaseException and types of exceptions via the
 * ExceptionType. DatabaseException could be raised with different
 * arguments: the cause, exception type, a message.
 *
 * @author Ganesh Ram Santhanam (gsanthan@cs.iastate.edu)
 * @author Bhavesh Sanghvi (bsanghvi@cs.iastate.edu)
 * @since 2007/11/14
 * @version $Date: 2008/02/03 18:33:43 $
 */
public class DatabaseException extends SQLException {

   /** The Constant serialVersionUID. */
   private static final long serialVersionUID = -4922903727376546938L;

   /**
    * Defines the various types of database exceptions.
    *
    * @author Ganesh Ram Santhanam (gsanthan@cs.iastate.edu)
    * @author Bhavesh Sanghvi (bsanghvi@cs.iastate.edu)
    * @since 2007/11/14
    * @version $Date: 2008/02/03 18:33:43 $
    */
   public enum ExceptionType {

      /** Constant to specify when a dirty connection is encountered. */
      DIRTY_CONNECTION("Encountered a dirty connection"),

      /** Constant to specify when unable to cconnect to the database. */
      UNABLE_TO_CONNECT("Unable to connect to the database"),

      /** Constant to specify when the exception is unknown. */
      UNKNOWN_TYPE("Unknown dabase exception");

      /** The message corresponding to the exception. */
      private final String message;

      /**
       * Instantiates a new exception type.
       *
       * @param message the message corresponding to the exception
       */
      private ExceptionType(String message) {
         this.message = message;
      }

      /* (non-Javadoc)
       * @see java.lang.Enum#toString()
       */
      @Override
      public String toString() {
         return getMessage();
      }

      /**
       * Gets the message.
       *
       * @return the message
       */
      public String getMessage() {
         return this.message;
      }

   }

   /** The exception type. */
   private final ExceptionType exceptionType;

   /**
    * Instantiates a new database exception.
    */
   public DatabaseException() {
      super();
      this.exceptionType = ExceptionType.UNKNOWN_TYPE;
   }

   /**
    * Instantiates a new database exception.
    *
    * @param message the message
    * @param cause the cause
    */
   public DatabaseException(String message, Throwable cause) {
      //super(message, cause);
      this.exceptionType = ExceptionType.UNKNOWN_TYPE;
   }

   /**
    * Instantiates a new database exception.
    *
    * @param message the message
    */
   public DatabaseException(String message) {
      super(message);
      this.exceptionType = ExceptionType.UNKNOWN_TYPE;
   }

   /**
    * Instantiates a new database exception.
    *
    * @param cause the cause
    */
   public DatabaseException(Throwable cause) {
      //super(cause);
      this.exceptionType = ExceptionType.UNKNOWN_TYPE;
   }

   /**
    * Instantiates a new database exception.
    *
    * @param type the type
    * @param message the message
    * @param cause the cause
    */
   public DatabaseException(ExceptionType type, String message, Throwable cause) {
      //super(message, cause);
      this.exceptionType = ExceptionType.UNKNOWN_TYPE;
   }

   /**
    * Instantiates a new database exception.
    *
    * @param exceptionType the exception type
    * @param message the message
    */
   public DatabaseException(ExceptionType exceptionType, String message) {
      super(message);
      this.exceptionType = exceptionType;
   }

   /**
    * Instantiates a new database exception.
    *
    * @param exceptionType the exception type
    * @param cause the cause
    */
   public DatabaseException(ExceptionType exceptionType, Throwable cause) {
      //super(cause);
      this.exceptionType = exceptionType;
   }

   /**
    * Instantiates a new database exception.
    *
    * @param exceptionType the exception type
    */
   public DatabaseException(ExceptionType exceptionType) {
      this.exceptionType = exceptionType;
   }

   /**
    * Gets the exception type.
    *
    * @return the exception type
    */
   public ExceptionType getExceptionType() {
      return this.exceptionType;
   }

}