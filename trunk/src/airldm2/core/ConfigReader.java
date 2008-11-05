/**
 * ConfigReader.java<br>
 * TODO Write description for ConfigReader.java.
 * 
 * $Header: $
 */

package airldm2.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import airldm2.constants.Constants;

/**
 * 
 * 
 * @author neeraj (neeraj@cs.iastate.edu, neeraj.kaul@gmail.com)
 * @since Nov 3, 2008
 * @version $Date: $
 */
public class ConfigReader {
   final Properties defaultProps = new Properties();

   static boolean configured = false;

   static ConfigReader reader;

   private ConfigReader() {

   }

   public static ConfigReader _this() {
      if (reader == null) {
         reader = new ConfigReader();
      }
      return reader;
   }

   public void init(String propertyFile) {

      FileInputStream in;
      try {
         in = new FileInputStream(propertyFile);
         try {
            defaultProps.load(in);
            in.close();
         } catch (IOException e) {

            e.printStackTrace();
         }
         configured = true;
      } catch (FileNotFoundException e) {

         System.out.println("File not found:" + propertyFile);
         e.printStackTrace();
      }

   }

   /**
    * Get the value associated with the key If the propertyFile has not
    * been initialized with call to init, default path is used
    * 
    * @param key
    * @return
    */
   public String getProperty(String key) {
      if (!configured) {
         init(Constants.DATABASE_PROPERTIES_RESOURCE_PATH);
      }
      return defaultProps.getProperty(key);

   }

}
