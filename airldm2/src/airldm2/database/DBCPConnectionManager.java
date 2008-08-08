/**
 * DBCPConnectionManager.java<br>
 * Realization of ConnectionManager interface using Apache DBCP commons
 * 
 * $Header:
 * /home/CVS/airldm2/src/airldm2/database/DBCPConnectionManager.java,v 1.2
 * 2008/02/05 19:47:22 neeraj Exp $
 */
package airldm2.database;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;

import airldm2.constants.Constants;
import airldm2.exceptions.DatabaseException;

public class DBCPConnectionManager extends ConnectionManager {

   /** The data source. */
   private DataSource dataSource = null;

   @Override
   public Connection createConnection() throws DatabaseException {
      Connection connection = null;

      try {
         connection = getDataSource().getConnection();

      } catch (DatabaseException e) {
         throw e;
      } catch (SQLException e) {
         throw new DatabaseException(
               DatabaseException.ExceptionType.UNABLE_TO_CONNECT, e);
      }

      return connection;
   }

   /**
    * Set up a basic data source
    * 
    * @param driverClassName
    * @param connectURI
    * @param username
    * @param password
    * @return
    * @throws Exception
    */
   public static DataSource setupDataSource(String driverClassName,
         String connectURI, String username, String password) throws Exception {
      final BasicDataSource basicDataSource = new BasicDataSource();
      basicDataSource.setDriverClassName(driverClassName);
      basicDataSource.setUrl(connectURI);
      basicDataSource.setUsername(username);
      basicDataSource.setPassword(password);
      return basicDataSource;
   }

   // TODO: Implement connection pooling as below
   // /**
   // * Set up a datasource with connection pooling
   // *
   // * @param connectURI - JDBC Connection URI
   // * @param username - JDBC Connection username
   // * @param password - JDBC Connection password
   // * @param minIdle - Minimum number of idel connection in the
   // connection
   // * pool
   // * @param maxActive - Connection Pool Maximum Capacity (Size)
   // * @throws Exception
   // */
   // public static DataSource setupPooledDataSource(String connectURI,
   // String
   // username,
   // String password, int minIdle, int maxActive) throws Exception {
   // //
   // // First, we'll need a ObjectPool that serves as the
   // // actual pool of connections.
   // //
   // // We'll use a GenericObjectPool instance
   // GenericObjectPool connectionPool = new GenericObjectPool(null);
   //
   // connectionPool.setMinIdle(minIdle);
   // connectionPool.setMaxActive(maxActive);
   //
   // // Next, we'll create a ConnectionFactory that the
   // // pool will use to create Connections.
   // // We'll use the DriverManagerConnectionFactory,
   //
   // ConnectionFactory connectionFactory = new
   // DriverManagerConnectionFactory(
   // connectURI, username, password);
   //
   // //
   // // Now we'll create the PoolableConnectionFactory, which wraps
   // // the "real" Connections created by the ConnectionFactory with
   // // the classes that implement the pooling functionality.
   // //
   // PoolableConnectionFactory poolableConnectionFactory = new
   // PoolableConnectionFactory(
   // connectionFactory, connectionPool, null, null, false, true);
   //
   // PoolingDataSource dataSource = new PoolingDataSource(connectionPool);
   //
   // return dataSource;
   // }

   /**
    * Lazy initialize the data source.
    * 
    * @return the data source
    * 
    * @throws DatabaseException the database exception
    */
   private DataSource getDataSource() throws DatabaseException {

      try {
         // if datasource has been set up before return it.
         if (dataSource != null)
            return this.dataSource;

         final Properties defaultProps = new Properties();
         final FileInputStream in = new FileInputStream(
               Constants.DATABASE_PROPERTIES_RESOURCE_PATH);
         defaultProps.load(in);
         in.close();

         final String driverClassName = defaultProps
               .getProperty("DataSource.driverClassName");
         final String url = defaultProps.getProperty("DataSource.url");
         final String username = defaultProps
               .getProperty("DataSource.username");
         String password = defaultProps.getProperty("DataSource.password");

         // make sure values are set. This function throws exception
         // otherwise
         isConfigured(driverClassName, url, username);

         if (isEmpty(password)) {
            password = "";
         }

         int minIdle = 10; // defaultValues
         int maxActive = 50; // defaultValues
         try {
            minIdle = Integer.parseInt(defaultProps
                  .getProperty("DataSource.minIdle"));
            maxActive = Integer.parseInt(defaultProps
                  .getProperty("DataSource.maxActive"));
         } catch (Exception ignore) {
         }
         /*
          * dataSource = setupPooledDataSource(url, username, password,
          * minIdle, maxActive);
          */

         dataSource = setupDataSource(driverClassName, url, username, password);

         return this.dataSource;
      } catch (DatabaseException e) {
         throw e;
      } catch (FileNotFoundException e) {
         throw new DatabaseException(
               DatabaseException.ExceptionType.UNABLE_TO_CONNECT, e);
      } catch (Exception e) {
         throw new DatabaseException(
               DatabaseException.ExceptionType.UNABLE_TO_CONNECT, e);
      }
   }

   private boolean isConfigured(String driverClassName, String url,
         String username) throws DatabaseException {

      if (isEmpty(driverClassName)) {
         throw new DatabaseException(
               DatabaseException.ExceptionType.UNABLE_TO_CONNECT,
               "Invalid data source driver class name.");
      } else if (isEmpty(url)) {
         throw new DatabaseException(
               DatabaseException.ExceptionType.UNABLE_TO_CONNECT,
               "Invalid data source url.");

      } else if (isEmpty(username)) {
         throw new DatabaseException(
               DatabaseException.ExceptionType.UNABLE_TO_CONNECT,
               "Invalid data source username.");
      } else {
         return true; // configuration is Okay
      }

   }

   private static boolean isEmpty(String s) {
      return (s == null || "".equals(s.trim()));

   }
}
