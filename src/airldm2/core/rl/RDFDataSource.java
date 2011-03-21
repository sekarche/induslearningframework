package airldm2.core.rl;

import java.util.List;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import airldm2.core.DefaultSufficentStatisticImpl;
import airldm2.core.ISufficentStatistic;
import airldm2.core.SSDataSource;
import airldm2.database.rdf.AggregationQueryConstructor;
import airldm2.database.rdf.IndependentValueAggregationQueryConstructor;
import airldm2.database.rdf.InstanceQueryConstructor;
import airldm2.database.rdf.RDFDatabaseConnection;
import airldm2.database.rdf.SuffStatQueryConstructor;
import airldm2.database.rdf.SuffStatQueryParameter;
import airldm2.database.rdf.ValueQueryConstructor;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.exceptions.RTConfigException;
import airldm2.util.AttribValuePair;
import airldm2.util.CollectionUtil;

public class RDFDataSource implements SSDataSource {

   private RDFDatabaseConnection mConn;
   private String mDefaultContext;

   public RDFDataSource(RDFDatabaseConnection conn, String defaultContext) {
      mConn = conn;
      mDefaultContext = defaultContext;
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
      
      List<Value[]> results = mConn.executeQuery(query);
      if (results.isEmpty()) return null;
      Value[] rv = results.get(0);
      
      int count = ((Literal)rv[0]).intValue();
      ISufficentStatistic stat = new DefaultSufficentStatisticImpl(count);
      return stat;
   }

   public List<URI> getTargetInstances(URI targetType) throws RDFDatabaseException {
      String query = new InstanceQueryConstructor(mDefaultContext, targetType).createQuery();
      //System.out.println(query);
      
      List<Value[]> results = mConn.executeQuery(query);
      List<URI> instances = CollectionUtil.makeList();
      for (Value[] rv : results) {
         if (rv[0] instanceof URI) {
            instances.add((URI) rv[0]);
         }
      }
      
      return instances;
   }
   
   public Value getValue(URI instance, RbcAttribute attribute) throws RDFDatabaseException {
      String query = new ValueQueryConstructor(mDefaultContext, instance, attribute).createQuery();
      //System.out.println(query);
      
      List<Value[]> results = mConn.executeQuery(query);
      if (results.isEmpty()) return null;
      Value[] rv = results.get(0);
      return rv[0];
   }
   
   public Value getAggregation(URI instance, RbcAttribute attribute) throws RDFDatabaseException {
      String query = new AggregationQueryConstructor(mDefaultContext, instance, attribute).createQuery();
      //System.out.println(query);
      
      List<Value[]> results = mConn.executeQuery(query);
      if (results.isEmpty()) return null;
      Value[] rv = results.get(0);
      return rv[0];
   }
   
   public int countIndependentValueAggregation(URI instance, RbcAttribute attribute, int v) throws RDFDatabaseException {
      String query = new IndependentValueAggregationQueryConstructor(mDefaultContext, instance, attribute, v).createQuery();
      //System.out.println(query);
      
      List<Value[]> results = mConn.executeQuery(query);
      if (results.isEmpty()) return 0;
      Value[] rv = results.get(0);
      return ((Literal)rv[0]).intValue();
   }
   
}
