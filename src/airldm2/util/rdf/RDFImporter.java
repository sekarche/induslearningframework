package airldm2.util.rdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import virtuoso.sesame2.driver.VirtuosoRepository;
import airldm2.constants.Constants;
import airldm2.exceptions.RTConfigException;

public class RDFImporter {

   public static void importFromFile(URI context, String file) throws RTConfigException, RepositoryException, RDFParseException, IOException {
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
      
      conn.clear(context);
      conn.add(new File(file), null, RDFFormat.RDFXML, context);
      conn.close();
   }

   public static void main(String[] args) throws RepositoryException, RDFParseException, RTConfigException, IOException {
      ValueFactory factory = new ValueFactoryImpl();
      URI context = factory.createURI(args[0]);
      importFromFile(context, args[1]);
   }
   
}
