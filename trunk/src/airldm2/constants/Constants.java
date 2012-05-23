package airldm2.constants;

public final class Constants {

   /** The Constant CONFIG_HOME. */
   // private static final String CONFIG_HOME = "config/";
   private static final String CONFIG_HOME = "";

   /** The Constant DATABASE_PROPERTIES_RESOURCE_PATH. */
   public static final String DATABASE_PROPERTIES_RESOURCE_PATH = CONFIG_HOME
         + "database.properties";
   
   public static final String RDFSTORE_PROPERTIES_RESOURCE_PATH = CONFIG_HOME + "rdfstore.properties";

   /* String representing ',' */
   public static final String COMMA = ",";

   /* String representing ';' */
   public static final String SEMI_COLON = ";";

   /* String representation of the quote charcter */
   public static final String SQL_QUOTE_CHAR = "`";

   // public static final String SQL_QUOTE_CHAR = "";

   /* String representation of the quote charcter */
   public static final String SQL_DATA_QUOTE_CHAR = "'";

   public static final double EPSILON = 0.000001;
   
}
