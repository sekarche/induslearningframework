package airldm2.util.rdf;

import java.io.IOException;
import java.util.List;

import javax.xml.transform.TransformerConfigurationException;

import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;
import org.xml.sax.SAXException;

import airldm2.classifiers.rl.ontology.TBox;
import airldm2.core.rl.RDFDataSource;
import airldm2.database.rdf.RDFDatabaseConnection;
import airldm2.database.rdf.VirtuosoConnection;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.exceptions.RTConfigException;
import airldm2.util.StringUtil;
import airldm2.util.Timer;

public class SubclassReasoner2 {

   public static void main(String[] args) throws RepositoryException, RTConfigException, RDFDatabaseException, TransformerConfigurationException, SAXException, IOException {
      RDFDatabaseConnection conn = new VirtuosoConnection("jdbc:virtuoso://localhost:1111/charset=UTF-8/log_enable=2", "dba", "dba");
      RDFDataSource dataSource = new RDFDataSource(conn, null);
      
      TBox tBox = dataSource.getTBox();
      
      Timer timer = new Timer();
      timer.start("Inf");
      for (URI c : tBox.getClasses()) {
         if (!c.stringValue().startsWith("http://cora")) continue;
         List<URI> sups = tBox.getSuperclasses(c);
         if (sups.isEmpty()) continue;
         
         StringBuilder query = new StringBuilder();
         query.append("INSERT INTO <:cora> { ");
         for (URI sup : sups) {
            query.append(StringUtil.triple("?x", "a", StringUtil.angleBracket(sup)));
         }         
         query.append(" } WHERE { ")
            .append(StringUtil.triple("?x", "a", StringUtil.angleBracket(c)))
            .append(" }");
         
         //System.out.println(query.toString());
         conn.executeUpdate(query.toString());
      }
      timer.stop("Inf");
   }
   
}
