package airldm2.database.rdf;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.openrdf.repository.RepositoryException;

import airldm2.constants.Constants;
import airldm2.exceptions.RTConfigException;

public class RDFDatabaseConnectionFactory {

   public static RDFDatabaseConnection makeFromConfig() throws RTConfigException, RepositoryException {
      final Properties defaultProps = new Properties();
      try {
         FileInputStream in = new FileInputStream(Constants.RDFSTORE_PROPERTIES_RESOURCE_PATH);
         defaultProps.load(in);
         in.close();
      } catch (IOException e) {
         throw new RTConfigException("Error reading " + Constants.RDFSTORE_PROPERTIES_RESOURCE_PATH, e);
      }
      
      String sparqlEndpointURL = defaultProps.getProperty("DataSource.sparqlEndpoint");
      if (sparqlEndpointURL != null) {
         return new VirtuosoConnection(sparqlEndpointURL);
         
      } else {
         String localURL = defaultProps.getProperty("DataSource.url");
         if (localURL != null) localURL = localURL.trim();
         
         String username = defaultProps.getProperty("DataSource.username");
         String password = defaultProps.getProperty("DataSource.password");
         return new VirtuosoConnection(localURL, username, password);
      }
   }
   
}
