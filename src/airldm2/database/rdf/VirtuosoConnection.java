package airldm2.database.rdf;

import java.util.List;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;

import virtuoso.sesame2.driver.VirtuosoRepository;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.util.CollectionUtil;

public class VirtuosoConnection implements RDFDatabaseConnection {
   
   private Repository mRepository;
   private RepositoryConnection mConn;
   
   public VirtuosoConnection(String sparqlEndpointURL) throws RepositoryException {
      mRepository = new HTTPRepository(sparqlEndpointURL);
      mRepository.initialize();
      mConn = mRepository.getConnection();
   }

   public VirtuosoConnection(String localURL, String username, String password) throws RepositoryException {
      mRepository = new VirtuosoRepository(localURL, username, password);
      mRepository.initialize();
      mConn = mRepository.getConnection();
   }
   
   @Override
   public void executeUpdate(String query) throws RDFDatabaseException {
      Update update;
      try {
         update = mConn.prepareUpdate(QueryLanguage.SPARQL, query);
      } catch (RepositoryException e) {
         throw new RDFDatabaseException(e);
      } catch (MalformedQueryException e) {
         throw new RDFDatabaseException(e);
      }
      
      try {
         update.execute();
      } catch (UpdateExecutionException e) {
         throw new RDFDatabaseException(e);
      }
   }
   
   @Override
   public SPARQLQueryResult executeQuery(String query) throws RDFDatabaseException {
      List<Value[]> results = CollectionUtil.makeList();
      
      TupleQuery resultsTable;
      try {
         resultsTable = mConn.prepareTupleQuery(QueryLanguage.SPARQL, query);
      } catch (RepositoryException e) {
         throw new RDFDatabaseException(e);
      } catch (MalformedQueryException e) {
         throw new RDFDatabaseException(e);
      }
      
      try {
         TupleQueryResult bindings = resultsTable.evaluate();
         List<String> names = bindings.getBindingNames();
         while (bindings.hasNext()) {
            BindingSet pairs = bindings.next();

            Value[] rv = new Value[names.size()];
            for (int i = 0; i < names.size(); i++) {
               String name = names.get(i);
               Value value = pairs.getValue(name);
               rv[i] = value;
            }

            results.add(rv);
         }

         bindings.close();
      } catch (QueryEvaluationException e) {
         throw new RDFDatabaseException(e);
      }
      
      return new SPARQLQueryResult(results);
   }
   
}
