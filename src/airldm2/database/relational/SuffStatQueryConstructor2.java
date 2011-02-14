/**
 * SuffStatQueryConstructor2.java<br>
 * TODO Write description for SuffStatQueryConstructor2.java.
 * 
 * $Header: $
 */

package airldm2.database.relational;

import airldm2.constants.Constants;
import airldm2.exceptions.RTConfigException;
import airldm2.util.AttribValuePair;

/**
 * TODO Write class description here.
 * 
 * @author neeraj (TODO Write email id here)
 * @since Nov 2, 2008
 * @version $Date: $
 */
public class SuffStatQueryConstructor2 {
   final static String SINGLE_ATTRIB_COUNT_TEMPLATE =

   "SELECT COUNT(*) FROM %tableName% WHERE %classification%='%classId%'"
         + Constants.SEMI_COLON;

   final static String MULTIPLE_ATTRIB_COUNT_TEMPLATE =

   "Select COUNT(*) FROM %tableName% WHERE %classification%='%classId%'  %MORE_ATTRIBS%"
         + Constants.SEMI_COLON;

   final static String MORE_ATTRIBS_TEMPLATE = " AND %attribName%='%attribValue%'";

   final static String NUMBER_INSTANCES_TEMPLATE = "SELECT COUNT(*) FROM  %tableName%"
         + Constants.SEMI_COLON;

   private static boolean quoteTables = false;

   public static String createCountQueryForAttribValue(String tableName,
         AttribValuePair value) throws RTConfigException {

      /*
       * String query = SINGLE_ATTRIB_COUNT_TEMPLATE; query =
       * query.replace( "%tableName%", "tableName"); query =
       * query.replace("%classification%", value.getAttribName()); query =
       * query.replace("%classId%", value.getAttribValue());
       */
      AttribValuePair[] values = new AttribValuePair[1];
      values[0] = value;
      return createCountQueryForAttribValues(tableName, values);

   }

   public static String createCountQueryForAttribValues(String tableName,
         AttribValuePair[] values) throws RTConfigException {

      if (!checkConfig(tableName, values)) {
         throw new RTConfigException(200,
               "Error while composing query from templates");
      }

      String query = MULTIPLE_ATTRIB_COUNT_TEMPLATE;
      if (quoteTables) {
         query = query.replace("%tableName%", Constants.SQL_QUOTE_CHAR
               + tableName + Constants.SQL_QUOTE_CHAR);
         query = query.replace("%classification%", Constants.SQL_QUOTE_CHAR
               + values[0].getAttribName() + Constants.SQL_QUOTE_CHAR);
         query = query.replace("%classId%", values[0].getAttribValue());
      } else {
         query = query.replace("%tableName%", tableName);
         query = query.replace("%classification%", values[0].getAttribName());
         query = query.replace("%classId%", values[0].getAttribValue());
      }

      String moreAttribs = "";

      for (int i = 1; i < values.length; i++) {
         moreAttribs += MORE_ATTRIBS_TEMPLATE; // set template
         if (quoteTables) {
            moreAttribs = moreAttribs.replace("%attribName%",
                  Constants.SQL_QUOTE_CHAR + values[i].getAttribName()
                        + Constants.SQL_QUOTE_CHAR);
         } else {
            moreAttribs = moreAttribs.replace("%attribName%", values[i]
                  .getAttribName());
         }
         moreAttribs = moreAttribs.replace("%attribValue%", values[i]
               .getAttribValue());
      }

      query = query.replace("%MORE_ATTRIBS%", moreAttribs);

      // System.out.println("Generated Query From Templates=" + query);

      // TODO: Verify it is a proper sql with some parser, say sql4j
      return query;
   }

   public static String createNumberInstancesQuery(String tableName) {
      String query = NUMBER_INSTANCES_TEMPLATE;
      query = query.replace("%tableName%", tableName);
      return query;

   }

   private static boolean checkConfig(String tableName, AttribValuePair[] value) {

      boolean okay = false;
      if (tableName != null && tableName != "") {
         if (value != null && value.length > 0) {
            okay = true;
         }
      }
      return okay;
   }

}
