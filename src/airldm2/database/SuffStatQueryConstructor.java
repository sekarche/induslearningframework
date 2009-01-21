package airldm2.database;

import airldm2.constants.Constants;
import airldm2.exceptions.RTConfigException;
import airldm2.util.AttribValuePair;

// TODO: Better Exceptions
public class SuffStatQueryConstructor {

   final static String SINGLE_ATTRIB_COUNT_TEMPLATE =

   "SELECT COUNT(*) FROM %tableName% WHERE %classification%=\"%classId%\""
         + Constants.SEMI_COLON;

   final static String MULTIPLE_ATTRIB_COUNT_TEMPLATE =

   "Select COUNT(*) FROM %tableName% WHERE %classification%=\"%classId%\"  %MORE_ATTRIBS%"
         + Constants.SEMI_COLON;

   final static String MORE_ATTRIBS_TEMPLATE = " AND %attribName%=\"%attribValue%\"";

   final static String MORE_ATTRIBS_MISSING_TEMPLATE = " AND %attribName% IN (\"%attribValue%\",\"%missingValue%\")";

   final static String NUMBER_INSTANCES_TEMPLATE = "SELECT COUNT(*) FROM  %tableName%"
         + Constants.SEMI_COLON;

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
               "Error while composing query frim templates");
      }

      String query = MULTIPLE_ATTRIB_COUNT_TEMPLATE;
      query = query.replace("%tableName%", Constants.SQL_QUOTE_CHAR + tableName
            + Constants.SQL_QUOTE_CHAR);

      query = query.replace("%classification%", Constants.SQL_QUOTE_CHAR
            + values[0].getAttribName() + Constants.SQL_QUOTE_CHAR);
      query = query.replace("%classId%", values[0].getAttribValue());

      String moreAttribs = "";

      for (int i = 1; i < values.length; i++) {
         if (values[i].IsIncludeMissingValue()) {
            moreAttribs += MORE_ATTRIBS_MISSING_TEMPLATE; // set template
         } else {
            moreAttribs += MORE_ATTRIBS_TEMPLATE; // set template
         }

         moreAttribs = moreAttribs.replace("%attribName%",
               Constants.SQL_QUOTE_CHAR + values[i].getAttribName()
                     + Constants.SQL_QUOTE_CHAR);
         moreAttribs = moreAttribs.replace("%attribValue%", values[i]
               .getAttribValue());
         if (values[i].IsIncludeMissingValue()) {
            moreAttribs = moreAttribs.replace("%missingValue%", values[i]
                  .getMissingValueRepresentation());
         }
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
