package airldm2.core;

import airldm2.exceptions.RTConfigException;
import airldm2.util.AttribValuePair;

/**
 * 
 * @author neeraj
 * 
 * Interface for a Sufficent Statistic DataSource
 */
public interface SSDataSource {

   /**
    * Utility method to pass a config Parameter which may be used while
    * setting the SSDataSource
    * 
    * @param config
    * @throws RTConfigException
    */
   public void init(String config) throws RTConfigException;

   /**
    * The new function that returns information from the data. The
    * information is returned as an ISufficientStatistic.
    * 
    * @param s A string in the form of an IndusSQL query.
    * @return an ISufficientStatistic that contains the needed data.
    */
   public ISufficentStatistic getSufficientStatistic(String s) throws Exception;

   /**
    * Populates an array of Sufficient Statistics based on the array of
    * requests.
    * 
    * @param s an array of IndusSQL queries.
    * @return The Sufficient Statistics corresponding to the requests, in
    * order.
    */
   public ISufficentStatistic[] getSufficientStatistic(String[] s)
         throws Exception;

   /**
    * Returns sufficent statistics for an attribute with the specified
    * value
    * 
    * @param attribValuePair The Object containing the attribName and
    * Values
    * @throws Exception if the attribName or attribValue is not part of the
    * DataSource Descriptor
    * @return ISufficentStatistic the sufficient statistic for the
    * attribValuePair
    */
   public ISufficentStatistic getSufficientStatistic(AttribValuePair nameValue)
         throws Exception;

   /**
    * Compute the sufficient statistics where the attribNames have the
    * given value. It computes sufficient statistics where all(logical AND)
    * the conditions(attribName=value) are met
    * 
    * @param nameValues An Array of Name Value pairs
    * @return
    * @throws Exception
    */
   public ISufficentStatistic getSufficientStatistic(
         AttribValuePair[] nameValues) throws Exception;

   /**
    * 
    * @return The Number of instances in this data source
    */
   public int getNumberInstances() throws Exception;

   /**
    * Set the relation in the dataSource against which sufficient
    * statistics are returned For a databse, it may be name of a table
    * 
    * @return
    */
   public void setRelationName(String relationName);

}
