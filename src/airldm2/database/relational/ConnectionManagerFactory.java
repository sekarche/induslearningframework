/**
 * ConnectionManagerFactory.java<br>
 * Factory for connection manager implementations
 *
 */
package airldm2.database.relational;

/**
 * Factory for connection manager implementations

 */
public class ConnectionManagerFactory {

   /**
    * Gets the connection manager.
    *
    * @return the connection manager
    */
   public static ConnectionManager getConnectionManager() {
      ConnectionManager manager = new DBCPConnectionManager();
      return manager;
   }

}
