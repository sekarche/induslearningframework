/**
 * SSDataSourceFactory.java<br>
 * TODO Write description for SSDataSourceFactory.java.
 * 
 * $Header: $
 */

package airldm2.core;

import airldm2.exceptions.RTConfigException;

/**
 * TODO Write class description here.
 * 
 * @author neeraj ( neeraj@cs.iastate.edu, neeraj.kaul@gmail.com)
 * @since Oct 28, 2008
 * @version $Date: $
 */
public class SSDataSourceFactory {

   // @SuppressWarnings("unchecked")
   public static SSDataSource getSSDataSourceImpl(String dataSourceType)
         throws RTConfigException {
      SSDataSource dataSourceImpl;

      try {
         if (dataSourceType.equals("relational")) {
            dataSourceImpl = (SSDataSource) Class.forName(
                  "airldm2.core.datatypes.relational.RelationalDataSource")
                  .newInstance();
         } else if (dataSourceType.equals("indus")) {
            dataSourceImpl = (SSDataSource) Class.forName(
                  "airldm2.core.datatypes.relational.IndusDataSource")
                  .newInstance();

         } else
            throw new RTConfigException(100,
                  "Could not create a Sufficient Statistics DataSource for unsupported option:"
                        + dataSourceType);
      } catch (Exception e) {
         e.printStackTrace(System.out);
         throw new RTConfigException(101,
               "SSDataSourceFactory Could not create the data Source"
                     + dataSourceType);

      }

      return dataSourceImpl;

   }
}