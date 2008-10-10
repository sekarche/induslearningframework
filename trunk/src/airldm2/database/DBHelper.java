/**
 * 
 */
package airldm2.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import airldm2.core.datatypes.relational.ColumnDescriptor;
import airldm2.core.datatypes.relational.TableDescriptor;
import airldm2.constants.Constants;

/**
 * @author neeraj
 * 
 * 
 */
public class DBHelper {
   // `airldmid` INT NOT NULL AUTO_INCREMENT PRIMARY KEY
   final static String CREATE_DATA_TABLE_TEMPLATE = " CREATE TABLE %tableName% (`airldmid` INT NOT NULL AUTO_INCREMENT PRIMARY KEY %attributes%)"
         + Constants.SEMI_COLON;

   final static String INSERT_VALUES_TEMPLATE = "INSERT INTO  %tableName% (%columnNames%)  VALUES(%values%)"
         + Constants.SEMI_COLON;

   final static String CREATE_TABLE_TEMPLATE = " CREATE TABLE %tableName% (%attributes%)"
         + Constants.SEMI_COLON;

   /*
    * The Suffix added to relation so as to get the tableName for its
    * descriptor
    */
   final static String DESC_TABLE_SUFFIX = "_desc";

   /**
    * A general method to execute a query in which the return type is a
    * single number (e.g. count queries)
    * 
    * @param query
    * @param con
    * @return
    * @throws SQLException
    */
   public static int ExecuteQuery(String query, Connection con)
         throws SQLException {
      Integer readInteger = null;
      ResultSet resultSet = null;
      Statement statement = con.createStatement();

      try {
         resultSet = statement.executeQuery(query);
         if (resultSet != null && resultSet.next()) {
            readInteger = resultSet.getInt(1);
         }

      } catch (SQLException e) {
         readInteger = null;
         throw e;
      } finally {
         ConnectionManager.close(resultSet);
      }

      return readInteger.intValue();

   }

   public static int ExecuteUpdateQuery(String query, Connection con)
         throws SQLException {

      Statement statement = con.createStatement();
      //System.out.println(query);
      return statement.executeUpdate(query);

   }

   /**
    * Creates a query to create a data descriptor. It contains a single row
    * with comma seperated values for that descriptor
    * 
    * @param desc
    * @return
    */
   public static String getCreateDataDescQuery(TableDescriptor desc) {
      String createTableQuery = CREATE_TABLE_TEMPLATE;
      String descTableName = desc.getTableName() + DESC_TABLE_SUFFIX;
      createTableQuery = createTableQuery
            .replace("%tableName%", Constants.SQL_QUOTE_CHAR + descTableName
                  + Constants.SQL_QUOTE_CHAR);

      String attributeTemplate = "%attributeName% VARCHAR(%size%)";

      String attrib = "";
      Integer size;
      Vector<ColumnDescriptor> attributes = desc.getColumns();
      for (ColumnDescriptor col : attributes) {

         attrib += attributeTemplate;
         attrib = attrib.replace("%attributeName%", Constants.SQL_QUOTE_CHAR
               + col.getColumnName() + Constants.SQL_QUOTE_CHAR);
         // In each column we are storing comma seperated values possible
         // for
         // that attribute
         size = new Integer((col.getNumValues())
               * col.getColumnSize().intValue() + col.getNumValues());
         // TODO: Add guard to see it does go beyond allowed size
         // VIKAS ADDED THIS

         if (size > 1000)
            size = 1000;
         attrib = attrib.replace("%size%", size.toString());
         attrib += Constants.COMMA;

      }

      attrib = removeTrailing(attrib, Constants.COMMA);

      createTableQuery = createTableQuery.replace("%attributes%", attrib);
      return createTableQuery;
   }

   public static String getCreateDataTableQuery(TableDescriptor desc) {
      String createTableQuery = CREATE_DATA_TABLE_TEMPLATE;
      createTableQuery = createTableQuery.replace("%tableName%",
            Constants.SQL_QUOTE_CHAR + desc.getTableName()
                  + Constants.SQL_QUOTE_CHAR);

      String attributeTemplate = "%attributeName% VARCHAR(%size%)";

      String attrib = "";
      Vector<ColumnDescriptor> attributes = desc.getColumns();
      for (ColumnDescriptor col : attributes) {
         attrib += Constants.COMMA; // seperate it from airldmid(default
         // added field)
         attrib += attributeTemplate;
         attrib = attrib.replace("%attributeName%", Constants.SQL_QUOTE_CHAR
               + col.getColumnName() + Constants.SQL_QUOTE_CHAR);
         attrib = attrib.replace("%size%", col.getColumnSize().toString());

      }
      createTableQuery = createTableQuery.replace("%attributes%", attrib);
      return createTableQuery;
   }

   public static String getInsertValuesQuery(TableDescriptor desc,
         Vector<String> values) {
      // currently no checking done to see if values being inserted are
      // those
      // defined in desc

      /*
       * INSERT_VALUES_TEMPLATE = "INSERT INTO %tableName% %columnNames%
       * VALUES(%values%)" + Constants.SEMI_COLON;
       */

      String insertQuery = INSERT_VALUES_TEMPLATE;
      insertQuery = insertQuery.replaceAll("%tableName%",
            Constants.SQL_QUOTE_CHAR + desc.getTableName()
                  + Constants.SQL_QUOTE_CHAR);
      insertQuery = insertQuery.replaceAll("%columnNames%",
            getCommaSeperatedColumns(desc.getColumns()));
      insertQuery = insertQuery.replaceAll("%values%",
            getCommaSeperatedValues(values));

      return insertQuery;

   }

   /**
    * Get a query where each of the possible values for a column/attribute
    * is inserted as comma seperated string for that attribute
    * 
    * @param desc
    * @return
    */
   public static String getInsertDescValuesQuery(TableDescriptor desc) {

      /*
       * INSERT_VALUES_TEMPLATE = "INSERT INTO %tableName% %columnNames%
       * VALUES(%values%)" + Constants.SEMI_COLON;
       */

      String insertQuery = INSERT_VALUES_TEMPLATE;
      String descTableName = desc.getTableName() + DESC_TABLE_SUFFIX;
      insertQuery = insertQuery
            .replaceAll("%tableName%", Constants.SQL_QUOTE_CHAR + descTableName
                  + Constants.SQL_QUOTE_CHAR);
      insertQuery = insertQuery.replaceAll("%columnNames%",
            getCommaSeperatedColumns(desc.getColumns()));
      String possibleValues = "";

      for (ColumnDescriptor col : desc.getColumns()) {
         possibleValues += Constants.SQL_DATA_QUOTE_CHAR
               + col.getCommaSeperatedPossibleValues()
               + Constants.SQL_DATA_QUOTE_CHAR + Constants.COMMA;
      }
      possibleValues = removeTrailing(possibleValues, Constants.COMMA);
      insertQuery = insertQuery.replaceAll("%values%", possibleValues);

      return insertQuery;

   }

   /**
    * Converts a Vector of Strings into comma seperated list(with quotes)
    * so ready to be inserted in a table
    * 
    * @param attributeValues
    * @return
    */
   private static String getCommaSeperatedValues(Vector<String> attributeValues) {
      String result = "";

      for (String currVal : attributeValues) {
         result += Constants.SQL_DATA_QUOTE_CHAR + currVal
               + Constants.SQL_DATA_QUOTE_CHAR + Constants.COMMA;
      }

      return removeTrailing(result, Constants.COMMA);

   }

   /**
    * 
    * @param attributes
    * @return A comma sperated list of the columns
    */
   private static String getCommaSeperatedColumns(
         Vector<ColumnDescriptor> attributes) {
      String result = "";

      for (ColumnDescriptor col : attributes) {
         result += Constants.SQL_QUOTE_CHAR + col.getColumnName()
               + Constants.SQL_QUOTE_CHAR + Constants.COMMA;

      }
      // return after removing trailing commas
      return removeTrailing(result, Constants.COMMA);
   }

   private static String removeTrailing(String st, String trailer) {
      // make call to utils to do this
      return airldm2.util.Utils.removeTrailing(st, trailer);

   }

}
