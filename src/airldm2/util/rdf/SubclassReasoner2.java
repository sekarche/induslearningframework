package airldm2.util.rdf;

import static airldm2.util.StringUtil.angleBracket;
import static airldm2.util.StringUtil.triple;

import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;

import airldm2.classifiers.rl.ontology.TBox;
import airldm2.core.rl.RDFDataSource;
import airldm2.database.rdf.RDFDatabaseConnection;
import airldm2.database.rdf.VirtuosoConnection;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.exceptions.RTConfigException;

public class SubclassReasoner2 {

   public static void main(String[] args) throws RepositoryException, RTConfigException, RDFDatabaseException {
      RDFDatabaseConnection conn = new VirtuosoConnection("jdbc:virtuoso://localhost:1111/charset=UTF-8/log_enable=2", "dba", "dba");
      RDFDataSource dataSource = new RDFDataSource(conn, null);
      
      TBox tBox = dataSource.getTBox();
      
      Timer timer = new Timer();
      timer.start("Inf");
      for (URI leaf : tBox.getLeaves()) {
         if (!leaf.stringValue().startsWith("http://:financial")) continue;
         
         StringBuilder query = new StringBuilder();
         query.append("INSERT INTO <:financial> { ");
         for (URI sup : tBox.getSuperclasses(leaf)) {
            query.append(triple("?x", "a", angleBracket(sup)));
         }         
         query.append(" } WHERE { ")
            .append(triple("?x", "a", angleBracket(leaf)))
            .append(" }");
         
         System.out.println(query.toString());
         conn.executeUpdate(query.toString());
      }
      timer.stop();
   }
   
}
