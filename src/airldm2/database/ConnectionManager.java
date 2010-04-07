/**
 * ConnectionManager.java<br>
 * Defines the APIs for Connection management
 * 
 * $Header: /home/CVS/airldm2/src/airldm2/database/ConnectionManager.java,v
 * 1.1 2008/02/03 18:33:43 neeraj Exp $
 */
package airldm2.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import airldm2.exceptions.DatabaseException;

/**
 * This abstract class defines APIs to interact with underlying JDBC
 * implementation: connection lifecycle management, statement management
 * and so on.
 * 
 */
public abstract class ConnectionManager {

   /**
    * Creates a new connection.
    * 
    * @return the connection object
    * 
    * @throws DatabaseException the database exception
    */
   public abstract Connection createConnection() throws DatabaseException;

   /**
    * Utility method to close the connection.
    * 
    * @see Connection#close()
    * 
    * @param connection the connection object to be closed
    */
   public static void close(final Connection connection) {
      if (connection != null) {
         try {
            connection.close();
         } catch (SQLException e) {
            // Suppress exception gracefully
            e.printStackTrace(); // TODO Replace with logger
         }
      }
   }

   /**
    * Utility method to close the statement.
    * 
    * @see Statement#close()
    * 
    * @param statement the statement object to be closed
    */
   public static void close(final Statement statement) {
      if (statement != null) {
         try {
            statement.close();
         } catch (SQLException e) {
            // Suppress exception gracefully
            e.printStackTrace(); // TODO Replace with logger
         }
      }
   }

   /**
    * Utility method to close the resultSet.
    * 
    * @see ResultSet#close()
    * 
    * @param statement the resultSet object to be closed
    */
   public static void close(final ResultSet resultSet) {
      if (resultSet != null) {
         try {
            resultSet.close();
         } catch (SQLException e) {
            // Suppress exception gracefully
            e.printStackTrace(); // TODO Replace with logger
         }
      }
   }

   /**
    * Commit.
    * 
    * @param connection the connection
    */
   public static void commit(final Connection connection) {
      if (connection != null) {
         try {
            connection.commit();
         } catch (SQLException e) {
            // Suppress exception gracefully
            e.printStackTrace(); // TODO Replace with logger
         }
      }
   }

   /**
    * Rollback.
    * 
    * @param connection the connection
    */
   public static void rollback(final Connection connection) {
      if (connection != null) {
         try {
            connection.rollback();
         } catch (SQLException e) {
            // Suppress exception gracefully
            e.printStackTrace(); // TODO Replace with logger
         }
      }
   }

}
