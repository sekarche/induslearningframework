package airldm2.core.rl;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openrdf.model.URI;
import org.openrdf.model.Value;

import airldm2.classifiers.rl.ontology.TBox;
import airldm2.core.DefaultSufficentStatisticImpl;
import airldm2.core.ISufficentStatistic;
import airldm2.core.SSDataSource;
import airldm2.database.rdf.AggregationQueryConstructor;
import airldm2.database.rdf.IndependentValueAggregationQueryConstructor;
import airldm2.database.rdf.InstanceQueryConstructor;
import airldm2.database.rdf.MultinomialSuffStatQueryConstructor;
import airldm2.database.rdf.RDFDatabaseConnection;
import airldm2.database.rdf.SPARQLQueryResult;
import airldm2.database.rdf.SuffStatQueryParameter;
import airldm2.database.rdf.SumSuffStatQueryConstructor;
import airldm2.database.rdf.ValueQueryConstructor;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.exceptions.RTConfigException;
import airldm2.util.ArrayUtil;
import airldm2.util.AttribValuePair;
import explore.database.rdf.CrawlPropertyQueryConstructor;
import explore.database.rdf.NestedAggregationQueryConstructor;
import explore.database.rdf.NestedAggregationQueryConstructor.Aggregator;
import explore.database.rdf.RangeQueryConstructor;
import explore.database.rdf.RangeSizeQueryConstructor;
import explore.database.rdf.RangeTypeQueryConstructor;
import explore.database.rdf.RangeTypeQueryConstructor.RangeType;
import explore.database.rdf.SubclassQueryConstructor;

public class RDFDataSource implements SSDataSource {

   private static Logger Log = Logger.getLogger("airldm2.core.rl.RDFDataSource");
   static { Log.setLevel(Level.WARNING); }
   
   private RDFDatabaseConnection mConn;
   private String mDefaultContext;
   private final RDFDataDescriptor mDesc;

   private TBox cTBox;
   
   public RDFDataSource(RDFDatabaseConnection conn, RDFDataDescriptor desc) {
      this(conn, desc, null);
   }
   
   public RDFDataSource(RDFDatabaseConnection conn, RDFDataDescriptor desc, String defaultContext) {
      mConn = conn;
      mDefaultContext = defaultContext;
      mDesc = desc;
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

   public ISufficentStatistic getMultinomialSufficientStatistic(SuffStatQueryParameter queryParam) throws RDFDatabaseException {
      String query = new MultinomialSuffStatQueryConstructor(mDesc, mDefaultContext, queryParam).createQuery();
      Log.info(query);
      
      SPARQLQueryResult results = mConn.executeQuery(query);
      if (results.isEmpty()) return null;
      ISufficentStatistic stat = new DefaultSufficentStatisticImpl(results.getInt());
      Log.info(String.valueOf(results.getInt()));
      return stat;
   }
   
   public ISufficentStatistic getSumSufficientStatistic(SuffStatQueryParameter queryParam) throws RDFDatabaseException {
      String query = new SumSuffStatQueryConstructor(mDesc, mDefaultContext, queryParam).createQuery();
      Log.info(query);
      
      SPARQLQueryResult results = mConn.executeQuery(query);
      if (results.isEmpty()) return null;
      
      ISufficentStatistic stat = null;
      if (results.isNull()) {
         stat = new DefaultSufficentStatisticImpl(0.0);
      } else {
         stat = new DefaultSufficentStatisticImpl(results.getDouble());
      }
      Log.info(String.valueOf(stat.getValue()));
      return stat;
   }

   public List<URI> getTargetInstances(URI targetType) throws RDFDatabaseException {
      String query = new InstanceQueryConstructor(mDesc, mDefaultContext, targetType).createQuery();
      Log.info(query);
      
      SPARQLQueryResult results = mConn.executeQuery(query);
      return results.getURIList();
   }
   
   public Value getValue(URI instance, RbcAttribute attribute) throws RDFDatabaseException {
      String query = new ValueQueryConstructor(mDesc, mDefaultContext, instance, attribute).createQuery();
      Log.info(query);
      
      SPARQLQueryResult results = mConn.executeQuery(query);
      return results.getValue();
   }
   
   public Value getAggregation(URI instance, RbcAttribute attribute) throws RDFDatabaseException {
      String query = new AggregationQueryConstructor(mDesc, mDefaultContext, instance, attribute).createQuery();
      Log.info(query);
      
      SPARQLQueryResult results = mConn.executeQuery(query);
      return results.getValue();
   }
   
   public int countIndependentValueAggregation(URI instance, RbcAttribute attribute, int v) throws RDFDatabaseException {
      String query = new IndependentValueAggregationQueryConstructor(mDesc, mDefaultContext, instance, attribute, v).createQuery();
      Log.info(query);
      
      SPARQLQueryResult results = mConn.executeQuery(query);
      if (results.isEmpty()) return 0;
      return results.getInt();
   }

   public int getRangeSizeOf(URI targetType, PropertyChain propChain) throws RDFDatabaseException {
      String query = new RangeSizeQueryConstructor(mDefaultContext, targetType, propChain).createQuery();
      Log.info(query);
      
      SPARQLQueryResult results = mConn.executeQuery(query);
      if (results.isEmpty()) return 0;
      return results.getInt();
   }
   
   public SPARQLQueryResult getRangeOf(URI targetType, PropertyChain propChain) throws RDFDatabaseException {
      String query = new RangeQueryConstructor(mDefaultContext, targetType, propChain).createQuery();
      Log.info(query);
      
      return mConn.executeQuery(query);
   }

   public List<URI> getPropertiesOf(URI targetType, PropertyChain propChain) throws RDFDatabaseException {
      String query = new CrawlPropertyQueryConstructor(mDefaultContext, targetType, propChain).createQuery();
      Log.info(query);
      
      SPARQLQueryResult results = mConn.executeQuery(query);
      return results.getURIList();
   }

   public RangeType getRangeTypeOf(URI targetType, PropertyChain propChain) throws RDFDatabaseException {
      String query = null;
      SPARQLQueryResult results = null;
      
      RangeType[] RANGE_TYPES = RangeType.values();
      int[] counts = new int[RANGE_TYPES.length];
      for (int i = 0; i < RANGE_TYPES.length; i++) {
         query = new RangeTypeQueryConstructor(mDefaultContext, targetType, propChain, RANGE_TYPES[i]).createQuery();
         results = mConn.executeQuery(query);
         counts[i] = results.getInt();   
      }
      
      int maxIndex = ArrayUtil.maxIndex(counts);
      return RANGE_TYPES[maxIndex];
   }

   public boolean isUniqueForInstance(URI targetType, PropertyChain propChain) throws RDFDatabaseException {
      String query = new NestedAggregationQueryConstructor(mDefaultContext, targetType, propChain, Aggregator.MAX, Aggregator.COUNT, false).createQuery();
      //Log.info(query);
      
      SPARQLQueryResult results = mConn.executeQuery(query);
      return results.getInt() <= 1;
   }

   public double getAverageForAggregation(URI targetType, PropertyChain propChain, Aggregator inner) throws RDFDatabaseException {
      boolean needsNumericFilter = !Aggregator.COUNT.equals(inner);
      String query = new NestedAggregationQueryConstructor(mDefaultContext, targetType, propChain, Aggregator.AVG, inner, needsNumericFilter).createQuery();
      //Log.info(query);
      
      SPARQLQueryResult results = mConn.executeQuery(query);
      return results.getDouble();
   }

   public TBox getTBox() throws RDFDatabaseException {
      if (cTBox == null) {
         cTBox = new TBox();
         
         String query = new SubclassQueryConstructor(mDefaultContext).createQuery();
         Log.info(query);
         SPARQLQueryResult results = mConn.executeQuery(query);
         List<Value[]> valueTupleList = results.getValueTupleList();
         for (Value[] vs : valueTupleList) {
            if (vs[0] instanceof URI && vs[1] instanceof URI) {
               cTBox.addSubclass((URI) vs[0], (URI) vs[1]);
            } else {
               System.err.println(Arrays.toString(vs));
            }
         }
         
         cTBox.computeClosure();
      }
      return cTBox;
   }
   
}
