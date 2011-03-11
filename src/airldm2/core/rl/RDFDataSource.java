package airldm2.core.rl;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import virtuoso.sesame2.driver.VirtuosoRepository;
import airldm2.constants.Constants;
import airldm2.core.DefaultSufficentStatisticImpl;
import airldm2.core.ISufficentStatistic;
import airldm2.core.SSDataSource;
import airldm2.database.rdf.AggregationQueryConstructor;
import airldm2.database.rdf.IndependentValueAggregationQueryConstructor;
import airldm2.database.rdf.InstanceQueryConstructor;
import airldm2.database.rdf.SuffStatQueryConstructor;
import airldm2.database.rdf.SuffStatQueryParameter;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.exceptions.RTConfigException;
import airldm2.util.AttribValuePair;
import airldm2.util.CollectionUtil;

public class RDFDataSource implements SSDataSource {

   private Repository mRepository;
   private RepositoryConnection mConn;
   private String mDefaultContext;

   public RDFDataSource(String trainGraph) throws RepositoryException, RTConfigException {
      mDefaultContext = trainGraph;
      
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

      mRepository = new VirtuosoRepository(url, username, password);
      mConn = mRepository.getConnection();
   }

   @Override
   public void init(String config) throws RTConfigException {
   }

   @Override
   public ISufficentStatistic getSufficientStatistic(String s) throws Exception {
      return null;
   }

   @Override
   public ISufficentStatistic[] getSufficientStatistic(String[] s)
         throws Exception {
      return null;
   }

   @Override
   public ISufficentStatistic getSufficientStatistic(AttribValuePair nameValue)
         throws Exception {
      return null;
   }

   @Override
   public ISufficentStatistic getSufficientStatistic(
         AttribValuePair[] nameValues) throws Exception {
      return null;
   }

   @Override
   public int getNumberInstances() throws Exception {
      return 0;
   }

   @Override
   public void setRelationName(String relationName) {
   }

   public ISufficentStatistic getSufficientStatistic(SuffStatQueryParameter queryParam) throws RDFDatabaseException {
      String query = new SuffStatQueryConstructor(mDefaultContext, queryParam).createQuery();
      System.out.println(query);
      
      List<Value[]> results = executeQuery(query);
      if (results.isEmpty()) return null;
      Value[] rv = results.get(0);
      
      int count = ((Literal)rv[0]).intValue();
      ISufficentStatistic stat = new DefaultSufficentStatisticImpl(count);
      return stat;
   }

   public List<URI> getTargetInstances(URI targetType) throws RDFDatabaseException {
      String query = new InstanceQueryConstructor(mDefaultContext, targetType).createQuery();
      System.out.println(query);
      
      List<Value[]> results = executeQuery(query);
      List<URI> instances = CollectionUtil.makeList();
      for (Value[] rv : results) {
         if (rv[0] instanceof URI) {
            instances.add((URI) rv[0]);
         }
      }
      
      return instances;
   }
   
   public Value getAggregation(URI instance, RbcAttribute attribute) throws RDFDatabaseException {
      String query = new AggregationQueryConstructor(mDefaultContext, instance, attribute).createQuery();
      System.out.println(query);
      
      List<Value[]> results = executeQuery(query);
      if (results.isEmpty()) return null;
      Value[] rv = results.get(0);
      return rv[0];
   }
   
   public int countIndependentValueAggregation(URI instance, RbcAttribute attribute, int v) throws RDFDatabaseException {
      String query = new IndependentValueAggregationQueryConstructor(mDefaultContext, instance, attribute, v).createQuery();
      System.out.println(query);
      
      List<Value[]> results = executeQuery(query);
      if (results.isEmpty()) return 0;
      Value[] rv = results.get(0);
      return ((Literal)rv[0]).intValue();
   }
   
   private List<Value[]> executeQuery(String query) throws RDFDatabaseException {
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
      
      return results;
   }
   
}
