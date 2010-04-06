/**
 * IndusDataSource.java<br>
 * TODO Write description for IndusDataSource.java.
 * 
 * $Header: $
 */

package airldm2.core.datatypes.relational;

import org.iastate.ailab.qengine.core.QueryEngine;
import org.iastate.ailab.qengine.core.QueryResult;

import airldm2.core.DefaultSufficentStatisticImpl;
import airldm2.core.ISufficentStatistic;
import airldm2.core.SSDataSource;
import airldm2.database.SuffStatQueryConstructor2;
import airldm2.exceptions.RTConfigException;
import airldm2.util.AttribValuePair;

/**
 * 
 * 
 * @author neeraj (neeraj.kaul@gmail.com, neeraj@cs.iastate.edu)
 * @since Oct 26, 2008
 * @version $Date: $
 */
public class IndusDataSource implements SSDataSource {

   QueryEngine engine;

   // initialize to -1 to show it has not been calculated
   int numberOfInstances = -1;

   String relationName = null;

   public void init(String baseDirectory) throws RTConfigException {
      try {
         engine = new QueryEngine(baseDirectory);
      } catch (Exception e) {

         throw new RTConfigException(
               "Error initializing Query Engine for Indus. Property indus.base is:"
                     + baseDirectory, e);
      }

   }

   /**
    * Constructor. indus.conf is assumed to be in directory pointed by
    * System.getProperty("user.dir");
    * 
    * @throws RTConfigException
    * 
    */

   public IndusDataSource() throws RTConfigException {
      /*
       * String base = ConfigReader._this().getProperty("indus_base");
       * 
       * try { engine = new QueryEngine(base); } catch (Exception e) {
       * 
       * throw new RTConfigException(
       * "Error initializing Query Engine for Indus. Property indus.base is:"
       * + base, e); }
       */
   }

   public IndusDataSource(String base) throws RTConfigException {
      try {
         engine = new QueryEngine(base);
      } catch (Exception e) {
         throw new RTConfigException(
               "Error initializing Query Engine for Indus", e);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see airldm2.core.SSDataSource#getNumberInstances()
    */
   public int getNumberInstances() throws Exception {
      return getNumberInstances(false);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * airldm2.core.SSDataSource#getSufficientStatistic(java.lang.String)
    */
   public ISufficentStatistic getSufficientStatistic(String countQuery)
         throws Exception {
      return executeIndusSuffStatQuery(countQuery);

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
      for (int i = 0; i < countQueries.length; i++) {
         stats[i] = executeIndusSuffStatQuery(countQueries[i]);
      }
      return stats;
   }

   /*
    * (non-Javadoc)
    * 
    * @seeairldm2.core.SSDataSource#getSufficientStatistic(airldm2.util.
    * AttribValuePair)
    */
   public ISufficentStatistic getSufficientStatistic(AttribValuePair nameValue)
         throws Exception {
      AttribValuePair[] values = new AttribValuePair[1];
      values[0] = nameValue;
      return getSufficientStatistic(values);
   }

   /*
    * (non-Javadoc)
    * 
    * @seeairldm2.core.SSDataSource#getSufficientStatistic(airldm2.util.
    * AttribValuePair[])
    */
   public ISufficentStatistic getSufficientStatistic(
         AttribValuePair[] nameValues) throws Exception {

      String countQuery = SuffStatQueryConstructor2
            .createCountQueryForAttribValues(relationName, nameValues);
      return executeIndusSuffStatQuery(countQuery);

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
         // String relationName = desc.getTableDesc().getTableName();
         String countQuery = SuffStatQueryConstructor2
               .createNumberInstancesQuery(relationName);
         numberOfInstances = executeIndusSuffStatQuery(countQuery).getValue()
               .intValue();

      }

      return numberOfInstances;

   }

   private ISufficentStatistic executeIndusSuffStatQuery(String countQuery)
         throws Exception {
      QueryResult result = engine.execute(countQuery);
      int count = result.getCount().intValue();
      double tempDouble = new Double(count).doubleValue();
      ISufficentStatistic stat = new DefaultSufficentStatisticImpl(tempDouble);
      return stat;
   }

   /**
    * Set the relationName. Needs to be called before Sufficient stat
    * queries can be answered
    */
   public void setRelationName(String relationName) {
      this.relationName = relationName;

   }

}
