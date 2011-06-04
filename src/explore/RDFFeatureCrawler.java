package explore;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.openrdf.model.URI;

import airldm2.core.rl.BinnedType;
import airldm2.core.rl.EnumType;
import airldm2.core.rl.NominalType;
import airldm2.core.rl.PropertyChain;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataDescriptorParser;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.core.rl.RbcAttribute.ValueAggregator;
import airldm2.core.rl.ValueType;
import airldm2.database.rdf.SPARQLQueryResult;
import airldm2.exceptions.RDFDataDescriptorFormatException;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.util.CollectionUtil;
import explore.database.rdf.NestedAggregationQueryConstructor.Aggregator;
import explore.database.rdf.RangeTypeQueryConstructor.RangeType;

public class RDFFeatureCrawler {

   private static final int MAX_PROPERTY_RANGE_FOR_FEATURE = 20;
   
   private RDFDataSource mDataSource;
   private RDFDataDescriptor cDesc;
   private int cMaxDepth;
   
   public RDFFeatureCrawler(RDFDataSource dataSource) {
      mDataSource = dataSource;
   }

   public void crawl(String inDescFile, String outDescFile, int maxDepth) throws IOException, RDFDataDescriptorFormatException, RDFDatabaseException {
      RDFDataDescriptor desc = RDFDataDescriptorParser.parse(inDescFile);
      crawl(desc, maxDepth);
      BufferedWriter out = new BufferedWriter(new FileWriter(outDescFile));
      desc.write(out);
      out.close();
   }
   
   public void crawl(RDFDataDescriptor desc, int maxDepth) throws RDFDatabaseException {
      cDesc = desc;
      cMaxDepth = maxDepth;
      List<PropertyChain> propChains = crawlPropertyChains();
      List<RbcAttribute> allAttributes = fillValueType(propChains);
      cDesc.addNonTargetAttributes(allAttributes);
   }

   private List<RbcAttribute> fillValueType(List<PropertyChain> propChains) throws RDFDatabaseException {
      List<RbcAttribute> allAttributes = CollectionUtil.makeList();
      for (PropertyChain p : propChains) {
         if (isPossibleFeature(p)) {
            List<RbcAttribute> attributes = makeAttributes(p);
            allAttributes.addAll(attributes);
         }
      }
      System.out.println(allAttributes);
      return allAttributes;
   }

   private boolean isPossibleFeature(PropertyChain propChain) throws RDFDatabaseException {
      int rangeSize = mDataSource.getRangeSizeOf(cDesc.getTargetType(), propChain);
      return rangeSize <= MAX_PROPERTY_RANGE_FOR_FEATURE;
   }

   private List<RbcAttribute> makeAttributes(PropertyChain propChain) throws RDFDatabaseException {
      List<RbcAttribute> allAttributes = CollectionUtil.makeList();
      int index = 1;
      RangeType rangeType = mDataSource.getRangeTypeOf(cDesc.getTargetType(), propChain);
      boolean isUnique = mDataSource.isUniqueForInstance(cDesc.getTargetType(), propChain);
      
      if (rangeType == RangeType.NUMERIC) {
         double average = 0.0;
         ValueType valueType = null;
         ValueAggregator valueAgg = null;
         
         average = mDataSource.getAverageForAggregation(cDesc.getTargetType(), propChain, Aggregator.AVG);
         valueType = new BinnedType(new double[] { average });
         valueAgg = ValueAggregator.AVG;
         allAttributes.add(new RbcAttribute(propChain.toString() + index++, propChain, valueType, valueAgg));
         
         if (!isUnique) {
            average = mDataSource.getAverageForAggregation(cDesc.getTargetType(), propChain, Aggregator.MIN);
            valueType = new BinnedType(new double[] { average });
            valueAgg = ValueAggregator.MIN;
            allAttributes.add(new RbcAttribute(propChain.toString() + index++, propChain, valueType, valueAgg));
            
            average = mDataSource.getAverageForAggregation(cDesc.getTargetType(), propChain, Aggregator.MAX);
            valueType = new BinnedType(new double[] { average });
            valueAgg = ValueAggregator.MAX;
            allAttributes.add(new RbcAttribute(propChain.toString() + index++, propChain, valueType, valueAgg));
         }
         
      } else {
         SPARQLQueryResult result = mDataSource.getRangeOf(cDesc.getTargetType(), propChain);
         if (isUnique) {
            if (rangeType == RangeType.IRI) {
               ValueType valueType = new EnumType(result.getURIList());
               ValueAggregator valueAgg = ValueAggregator.NONE;
               allAttributes.add(new RbcAttribute(propChain.toString() + index++, propChain, valueType, valueAgg));
            } else if (rangeType == RangeType.STRING) {
               ValueType valueType = new NominalType(result.getStringList());
               ValueAggregator valueAgg = ValueAggregator.NONE;
               allAttributes.add(new RbcAttribute(propChain.toString() + index++, propChain, valueType, valueAgg));
            }
         } else {
            if (rangeType == RangeType.IRI) {
               ValueType valueType = new EnumType(result.getURIList());
               ValueAggregator valueAgg = ValueAggregator.INDEPENDENT_VAL;
               allAttributes.add(new RbcAttribute(propChain.toString() + index++, propChain, valueType, valueAgg));
            } else if (rangeType == RangeType.STRING) {
               ValueType valueType = new NominalType(result.getStringList());
               ValueAggregator valueAgg = ValueAggregator.INDEPENDENT_VAL;
               allAttributes.add(new RbcAttribute(propChain.toString() + index++, propChain, valueType, valueAgg));
            }
         }
      }
      
      if (!isUnique) {
         double average = mDataSource.getAverageForAggregation(cDesc.getTargetType(), propChain, Aggregator.COUNT);
         ValueType valueType = new BinnedType(new double[] { average });
         ValueAggregator valueAgg = ValueAggregator.COUNT;
         allAttributes.add(new RbcAttribute(propChain.toString() + index++, propChain, valueType, valueAgg));
      }
      
      return allAttributes;
   }

   private List<PropertyChain> crawlPropertyChains() throws RDFDatabaseException {
      List<PropertyChain> allPropertyChains = CollectionUtil.makeList();
      List<PropertyChain> currentDepth = crawlNextDepth(new PropertyChain());
      allPropertyChains.addAll(currentDepth);
      for (int i = 0; i < cMaxDepth; i++) {
         for (PropertyChain c : currentDepth) {
            currentDepth = crawlNextDepth(c);
         }
         allPropertyChains.addAll(currentDepth);
      }      
      
      allPropertyChains.remove(cDesc.getTargetAttribute().getProperties());
      return allPropertyChains;
   }
   
   private List<PropertyChain> crawlNextDepth(PropertyChain propChain) throws RDFDatabaseException {
      List<URI> props = mDataSource.getPropertiesOf(cDesc.getTargetType(), propChain);
      List<PropertyChain> nextDepth = CollectionUtil.makeList();
      for (URI prop : props) {
         nextDepth.add(propChain.append(prop));
      }
      return nextDepth;
   }
   
}
