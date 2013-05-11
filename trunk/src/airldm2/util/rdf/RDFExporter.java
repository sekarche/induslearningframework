package airldm2.util.rdf;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.ntriples.NTriplesWriter;

import virtuoso.sesame2.driver.VirtuosoRepository;
import airldm2.constants.Constants;
import airldm2.exceptions.RTConfigException;

public class RDFExporter {

   public static void exportFromFile(URI context, String file) throws RTConfigException, RepositoryException, RDFParseException, IOException, RDFHandlerException {
      final Properties defaultProps = new Properties();
      try {
         FileInputStream in = new FileInputStream(Constants.RDFSTORE_PROPERTIES_RESOURCE_PATH);
         defaultProps.load(in);
         in.close();
      } catch (IOException e) {
         throw new RTConfigException("Error reading " + Constants.RDFSTORE_PROPERTIES_RESOURCE_PATH, e);
      }

      String url = defaultProps.getProperty("DataSource.url");
      if (url != null) url = url.trim();
      String username = defaultProps.getProperty("DataSource.username");
      String password = defaultProps.getProperty("DataSource.password");

      Repository repository = new VirtuosoRepository(url, username, password);
      RepositoryConnection conn = repository.getConnection();
      
      conn.export(new NTriplesWriter(new FileWriter(file)), context);
   }

   public static void main(String[] args) throws RepositoryException, RDFParseException, RTConfigException, IOException, RDFHandlerException {
      ValueFactory factory = new ValueFactoryImpl();
      URI context = factory.createURI(args[1]);
      exportFromFile(context, args[0]);
   }
   
}
