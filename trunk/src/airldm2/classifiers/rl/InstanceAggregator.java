package airldm2.classifiers.rl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import airldm2.classifiers.rl.estimator.AttributeValue;
import airldm2.classifiers.rl.estimator.Category;
import airldm2.classifiers.rl.estimator.MappedHistogram;
import airldm2.classifiers.rl.estimator.Null;
import airldm2.classifiers.rl.estimator.Numeric;
import airldm2.core.LDInstances;
import airldm2.core.rl.DiscreteType;
import airldm2.core.rl.NumericType;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.core.rl.ValueAggregator;
import airldm2.core.rl.ValueType;
import airldm2.database.rdf.SuffStatQueryParameter;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.util.CollectionUtil;

public class InstanceAggregator {
   
   public static AggregatedInstances init(LDInstances instances, double samplePerc) throws RDFDatabaseException {
      RDFDataSource dataSource = (RDFDataSource) instances.getDataSource();
      RDFDataDescriptor dataDesc = (RDFDataDescriptor) instances.getDesc();
      
      List<URI> instanceURIs = null;
      if (samplePerc >= 1.0) {
         instanceURIs = dataSource.getTargetInstances(dataDesc.getTargetType());
      } else {
         instanceURIs = CollectionUtil.makeList();
         Random rnd = new Random(0);
         RbcAttribute targetAttribute = dataDesc.getTargetAttribute();
         for (int j = 0; j < targetAttribute.getDomainSize(); j++) {
            SuffStatQueryParameter queryParam = new SuffStatQueryParameter(dataDesc.getTargetType(), targetAttribute, j);
            List<URI> classInstances = dataSource.getTargetInstances(queryParam);
            Collections.shuffle(classInstances, rnd);
            
            int size = (int) (classInstances.size() * samplePerc);
            if (size <= 0) size = 1;
            List<URI> subList = classInstances.subList(0, size);
            instanceURIs.addAll(subList);
         }
      }

      AggregatedInstances ais = new AggregatedInstances(instanceURIs);
      
      List<AggregatedInstance> aggInstances = CollectionUtil.makeList();
      for (URI instanceURI : instanceURIs) {
         RbcAttribute targetAttribute = dataDesc.getTargetAttribute();
         
         Category targetCategory = (Category) aggregateAttribute(dataSource, instanceURI, targetAttribute);
         
         AggregatedInstance aggInstance = new AggregatedInstance(instanceURI, targetCategory);
         aggInstances.add(aggInstance);
      }
      ais.setInstances(aggInstances);
      return ais;
   }

   public static AggregatedInstances aggregateAll(LDInstances instances) throws RDFDatabaseException {
      return aggregateSample(instances, 1.0);
   }
   
   public static AggregatedInstances aggregateSample(LDInstances instances, double samplePerc) throws RDFDatabaseException {
      AggregatedInstances ais = init(instances, samplePerc);
      
      RDFDataSource dataSource = (RDFDataSource) instances.getDataSource();
      RDFDataDescriptor dataDesc = (RDFDataDescriptor) instances.getDesc();
      List<RbcAttribute> nonTargetAttributes = dataDesc.getNonTargetAttributeList();
      
      for (RbcAttribute att : nonTargetAttributes) {
         List<AttributeValue> values = aggregateAttributeForInstances(dataSource, ais.getURIs(), att);
         ais.addAttribute(att, values);
      }
            
      return ais;
   }
   

   public static List<AttributeValue> aggregateAttributeForInstances(RDFDataSource dataSource, List<URI> instances, RbcAttribute att) throws RDFDatabaseException {
      List<AttributeValue> values = CollectionUtil.makeList();
      for (URI instanceURI : instances) {
         try {
            AttributeValue value = aggregateAttribute(dataSource, instanceURI, att);
            values.add(value);
         } catch (IllegalArgumentException ex) {
            System.err.print(".");
            continue;
         }
      }
      
      return values;
   }

   private static AttributeValue aggregateAttribute(RDFDataSource dataSource, URI instance, RbcAttribute attribute) throws RDFDatabaseException {
      ValueType valueType = attribute.getValueType();
      
      if (attribute.getAggregatorType() == ValueAggregator.HISTOGRAM) {
         DiscreteType dt = (DiscreteType) valueType;
         List<String> domain = dt.getStringValues();
         Map<String, Integer> counts = dataSource.countHistogramAggregation(instance, attribute);
         double[] valueIndexCount = new double[dt.domainSize()];
         for (int i = 0; i < valueIndexCount.length; i++) {
            String strValue = domain.get(i);
            Integer count = counts.get(strValue);
            if (count == null) count = 0;
            valueIndexCount[i] = count;
         }
         
         return new MappedHistogram(valueIndexCount, counts);
         
      } else if (attribute.getAggregatorType() == ValueAggregator.SET) {
         DiscreteType dt = (DiscreteType) valueType;
         List<String> domain = dt.getStringValues();
         Map<String, Integer> counts = dataSource.countHistogramAggregation(instance, attribute);
         double[] valueIndexCount = new double[dt.domainSize()];
         for (int i = 0; i < valueIndexCount.length; i++) {
            String strValue = domain.get(i);
            Integer count = counts.get(strValue);
            if (count == null) count = 0;
            if (count > 0) {
               valueIndexCount[i] = 1;
            } else {
               valueIndexCount[i] = 0;
            }
         }
         
         return new MappedHistogram(valueIndexCount, counts);
         
      } else if (attribute.getAggregatorType() == ValueAggregator.NONE) {
         Value value = dataSource.getValue(instance, attribute);
         if (value == null) {
            //throw new IllegalArgumentException("Missing " + attribute.getName() + " value for instance " + instance);
            //System.err.println("Missing " + attribute.getName() + " value for instance " + instance);
            return new Null();
         }
         
         if (valueType instanceof NumericType) {
            Literal lit = (Literal) value;
            double doubleValue = lit.doubleValue();
            return new Numeric(doubleValue);
         } else {
            DiscreteType dt = (DiscreteType) valueType;
            int index = dt.indexOf(value);
            return new Category(index);
         }
         
      } else {
         Value value = dataSource.getAggregation(instance, attribute);
         if (value == null) {
            //throw new IllegalArgumentException("Missing " + attribute.getName() + " value for instance " + instance);
            //System.err.println("Missing " + attribute.getName() + " value for instance " + instance);
            return new Null();
         }
         
         if (valueType instanceof NumericType) {
            Literal lit = (Literal) value;
            double doubleValue = lit.doubleValue();
            return new Numeric(doubleValue);
         } else {
            DiscreteType dt = (DiscreteType) valueType;
            int index = dt.indexOf(value);
            return new Category(index);
         }
      }
   }

}
