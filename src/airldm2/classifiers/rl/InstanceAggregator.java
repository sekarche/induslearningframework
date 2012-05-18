package airldm2.classifiers.rl;

import java.util.List;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import airldm2.classifiers.rl.estimator.AttributeValue;
import airldm2.classifiers.rl.estimator.Category;
import airldm2.classifiers.rl.estimator.Histogram;
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
import airldm2.exceptions.RDFDatabaseException;
import airldm2.util.CollectionUtil;

public class InstanceAggregator {
   
   public static AggregatedInstances init(LDInstances instances) throws RDFDatabaseException {
      RDFDataSource dataSource = (RDFDataSource) instances.getDataSource();
      RDFDataDescriptor dataDesc = (RDFDataDescriptor) instances.getDesc();
      
      List<URI> instanceURIs = dataSource.getTargetInstances(dataDesc.getTargetType());
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
      AggregatedInstances ais = init(instances);
      
      RDFDataSource dataSource = (RDFDataSource) instances.getDataSource();
      RDFDataDescriptor dataDesc = (RDFDataDescriptor) instances.getDesc();
      List<RbcAttribute> nonTargetAttributes = dataDesc.getNonTargetAttributeList();
      
      for (RbcAttribute att : nonTargetAttributes) {
         List<AttributeValue> values = aggregateAttributeForInstances(dataSource, ais.getURIs(), att);
         ais.addAttribute(values);
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
         double[] valueIndexCount = new double[dt.domainSize()];
         
         for (int i = 0; i < valueIndexCount.length; i++) {
            valueIndexCount[i] = dataSource.countIndependentValueAggregation(instance, attribute, i);
         }
         return new Histogram(valueIndexCount);
         
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
