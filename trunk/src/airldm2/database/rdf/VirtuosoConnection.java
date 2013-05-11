package airldm2.database.rdf;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import airldm2.util.Timer;
import airldm2.util.Weigher;

public class VirtuosoConnection implements RDFDatabaseConnection {
   
   private Repository mRepository;
   private RepositoryConnection mConn;
   
   private String mLocalURL;
   private String mUsername;
   private String mPassword;
   
   private final int QUERY_LIMIT = 9000;
   private int mQueryCount = 0;
   
   private static Logger Log = Logger.getLogger("airldm2.core.rl.VirtuosoConnection");
   static { Log.setLevel(Level.WARNING); }
   
   public VirtuosoConnection(String sparqlEndpointURL) throws RepositoryException {
      mRepository = new HTTPRepository(sparqlEndpointURL);
      mRepository.initialize();
      mConn = mRepository.getConnection();
   }

   public VirtuosoConnection(String localURL, String username, String password) throws RepositoryException {
      this.mLocalURL = localURL;
      this.mUsername = username;
      this.mPassword = password;
      init(localURL, username, password);
   }

   private void init(String localURL, String username, String password) throws RepositoryException {
      mRepository = new VirtuosoRepository(localURL, username, password);
      mRepository.initialize();
      mConn = mRepository.getConnection();
   }
   
   public void close() throws RepositoryException {
      mConn.close();
      mRepository.shutDown();
   }
   
   @Override
   public void executeUpdate(String query) throws RDFDatabaseException {
      Log.info(query);
      
      Weigher.INSTANCE.add(query);
      
      Update update;
      try {
         update = mConn.prepareUpdate(QueryLanguage.SPARQL, query);
      } catch (RepositoryException e) {
         throw new RDFDatabaseException(e);
      } catch (MalformedQueryException e) {
         throw new RDFDatabaseException(e);
      }
      
      try {
         Timer.INSTANCE.start("Query");
         update.execute();
         Timer.INSTANCE.stop("Query");
      } catch (UpdateExecutionException e) {
         throw new RDFDatabaseException(e);
      }
   }
   
   @Override
   public SPARQLQueryResult executeQuery(String query) throws RDFDatabaseException {
      Log.info(query);
      
      Weigher.INSTANCE.add(query);
      
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
         //Timer.INSTANCE.start("Query");
         TupleQueryResult bindings = resultsTable.evaluate();
         //Timer.INSTANCE.stop("Query");
         
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
      
      Weigher.INSTANCE.add(results);
      
      try {
         manageQueryCount();
      } catch (RepositoryException e) {
         throw new RDFDatabaseException(e);
      }
      
      return new SPARQLQueryResult(results);
   }

   private void manageQueryCount() throws RepositoryException {
      if (mLocalURL == null) return;
      
      mQueryCount++;
      if (mQueryCount > QUERY_LIMIT) {
         mQueryCount = 0;
         close();
         init(mLocalURL, mUsername, mPassword);
      }
   }
   
}
