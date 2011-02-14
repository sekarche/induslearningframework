/**
 * 
 */
package airldm2.core.datatypes.relational;

import java.sql.Connection;

import airldm2.core.DefaultSufficentStatisticImpl;
import airldm2.core.ISufficentStatistic;
import airldm2.core.SSDataSource;
import airldm2.database.relational.DBCPConnectionManager;
import airldm2.database.relational.SuffStatQueryConstructor;
import airldm2.exceptions.DatabaseException;
import airldm2.exceptions.RTConfigException;
import airldm2.util.AttribValuePair;

/**
 * @author neeraj (neeraj.kaul@gmail.com, neeraj@cs.iastate.edu)
 * 
 */
public class RelationalDataSource implements SSDataSource {

   Connection con;

   String relationName;

   // initialize to -1 to show it has not been calculated
   int numberOfInstances = -1;

   /**
    * Empty method as NO specific configuration required . Here to satisfy
    * interface implementation
    */
   public void init(String configFile) {
   }

   /**
    * A constructor Sets up the connection to the database using
    * ConnectionManger
    * 
    * @param relationName The table in which the relation exists.
    */
   public RelationalDataSource(String relationName) throws RTConfigException,
         DatabaseException {
      if (relationName == null || relationName.trim().equals("")) {
         throw new RTConfigException(300,
               "A RelationalDataSource initialized with empty relation Name");
      }
      this.relationName = relationName;
      /*
       * 
       * try { Class.forName("com.mysql.jdbc.Driver").newInstance(); con =
       * DriverManager.getConnection(
       * "jdbc:mysql://129.186.93.141/db_research", "indus", "indus"); }
       * catch (Exception e) { throw new DatabaseException(
       * DatabaseException.ExceptionType.UNABLE_TO_CONNECT, e); }
       */
      con = new DBCPConnectionManager().createConnection();
   }

   /**
    * Empty param constructor . Currently used for creating instances via
    * Class.forName()
    * 
    * @throws DatabaseException
    */
   public RelationalDataSource() throws DatabaseException {
      con = new DBCPConnectionManager().createConnection();

   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * airldm2.core.SSDataSource#getSufficientStatistic(java.lang.String)
    */
   public ISufficentStatistic getSufficientStatistic(String query)
         throws Exception {

      String[] queries = new String[1];
      queries[0] = query;
      return getSufficientStatistic(queries)[0];
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * airldm2.core.SSDataSource#getSufficientStatistic(java.lang.String[])
    */
   public ISufficentStatistic[] getSufficientStatistic(String[] countQueries)
         throws Exception {
      ISufficentStatistic[] stats = new DefaultSufficentStatisticImpl[countQueries.length];
      int count;
      double tempDouble;
      for (int i = 0; i < countQueries.length; i++) {
         count = airldm2.database.relational.DBHelper.ExecuteQuery(countQueries[i], con);

         tempDouble = new Double(count).doubleValue();
         stats[i] = new DefaultSufficentStatisticImpl(tempDouble);
      }

      return stats;
   }

   /**
    * Returns sufficient statistics for an attribute with the specified
    * value
    * 
    * @param nameValue
    * @throws Exception if the attribName or attribValue is not part of the
    * DataSource Descriptor
    * @return
    */
   public ISufficentStatistic getSufficientStatistic(AttribValuePair nameValue)
         throws Exception {

      AttribValuePair[] values = new AttribValuePair[1];
      values[0] = nameValue;
      return getSufficientStatistic(values);

   }

   /**
    * 
    * @param nameValues
    * @return
    * @throws Exception
    */
   public ISufficentStatistic getSufficientStatistic(
         AttribValuePair[] nameValues) throws Exception {

      String countQuery = SuffStatQueryConstructor
            .createCountQueryForAttribValues(relationName, nameValues);
      int count = airldm2.database.relational.DBHelper.ExecuteQuery(countQuery, con);
      double tempDouble = new Double(count).doubleValue();
      ISufficentStatistic stat = new DefaultSufficentStatisticImpl(tempDouble);
      return stat;
   }

   public int getNumberInstances() throws Exception {
      return getNumberInstances(false);
   }

   /**
    * 
    * @param force forces a query to the data source instead of using the
    * stored value.
    * @return
    * @throws Exception
    */
   public int getNumberInstances(boolean force) throws Exception {
      if (numberOfInstances == -1 || force) {
         String countQuery = SuffStatQueryConstructor
               .createNumberInstancesQuery(relationName);
         numberOfInstances = airldm2.database.relational.DBHelper.ExecuteQuery(countQuery,
               con);
      }

      return numberOfInstances;

   }

   // public RelationalDataSource(File arrfFilePath) throws Exception {
   // TODO Call Online ArrfReader to do the needful

   // }

   public void setRelationName(String relationName) {
      this.relationName = relationName;

   }

}
