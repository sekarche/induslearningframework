package airldm2.util.rdf;

import java.util.List;
import java.util.Random;

import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;

import airldm2.database.rdf.RDFDatabaseConnection;
import airldm2.database.rdf.SPARQLQueryResult;
import airldm2.database.rdf.VirtuosoConnection;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.exceptions.RTConfigException;

import com.clarkparsia.pellint.util.CollectionUtil;

public class Sampler {

   public static void main(String[] args) throws RepositoryException, RTConfigException, RDFDatabaseException {
      RDFDatabaseConnection conn = new VirtuosoConnection("jdbc:virtuoso://localhost:1111/charset=UTF-8/log_enable=2", "dba", "dba");

      String query = "SELECT ?x FROM <:financial> WHERE { ?x <http://lisp.vse.cz/pkdd99/vocab/resource/loan_status> \"A\" . }";
      SPARQLQueryResult result = conn.executeQuery(query);
      List<URI> uriList = result.getURIList();
      List<URI> toRemove = CollectionUtil.makeList();
      
      Random r = new Random();
      while (uriList.size() > 324) {
         int nextInt = r.nextInt(uriList.size());
         URI removed = uriList.remove(nextInt);
         toRemove.add(removed);
      }
      
      for (URI i : toRemove) {
         StringBuilder deleteQuery = new StringBuilder();
         deleteQuery.append("DELETE FROM <:financial> { <" + i.toString() + "> ?y ?z } WHERE { <" + i.toString() + "> ?y ?z . }");
     
         conn.executeUpdate(deleteQuery.toString());
      }
   }
}
