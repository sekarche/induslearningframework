package airldm2.classifiers.rl;

import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.Value;

import airldm2.core.LDInstances;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.core.rl.RbcAttribute.ValueAggregator;
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
         
         ValueIndexCount targetValueIndexCount = null;
         try {
            targetValueIndexCount = aggregateAttribute(dataSource, instanceURI, targetAttribute);
         } catch (IllegalArgumentException ex) {
            System.err.print(".");
            continue;
         }
         
         AggregatedInstance aggInstance = new AggregatedInstance(instanceURI, targetValueIndexCount);
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
         List<ValueIndexCount> indexCounts = aggregateAttributeForInstances(dataSource, ais.getURIs(), att);
         ais.addAttribute(indexCounts);
      }
            
      return ais;      
   }

   public static List<ValueIndexCount> aggregateAttributeForInstances(RDFDataSource dataSource, List<URI> instances, RbcAttribute att) throws RDFDatabaseException {
      List<ValueIndexCount> indexCounts = CollectionUtil.makeList();
      for (URI instanceURI : instances) {
         try {
            ValueIndexCount indexCount = aggregateAttribute(dataSource, instanceURI, att);
            indexCounts.add(indexCount);
         } catch (IllegalArgumentException ex) {
            System.err.print(".");
            continue;
         }
      }
      
      return indexCounts;
   }
   
   private static ValueIndexCount aggregateAttribute(RDFDataSource dataSource, URI instance, RbcAttribute attribute) throws RDFDatabaseException {
      ValueType valueType = attribute.getValueType();
      int[] valueIndexCount = new int[valueType.domainSize()];
      
      if (attribute.getAggregatorType() == ValueAggregator.INDEPENDENT_VAL) {
         for (int i = 0; i < valueIndexCount.length; i++) {
            valueIndexCount[i] = dataSource.countIndependentValueAggregation(instance, attribute, i);
         }
         
      } else if (attribute.getAggregatorType() == ValueAggregator.NONE) {
         Value value = dataSource.getValue(instance, attribute);
         if (value == null) {
            //throw new IllegalArgumentException("Missing " + attribute.getName() + " value for instance " + instance);
            //System.err.println("Missing " + attribute.getName() + " value for instance " + instance);
         } else {
            int index = valueType.indexOf(value);
            if (index >= 0) {
               valueIndexCount[index] = 1;
            }
         }
         
      } else {
         Value aggregatedValue = dataSource.getAggregation(instance, attribute);
         int index = valueType.indexOf(aggregatedValue);
         if (index >= 0) {
            valueIndexCount[index] = 1;
         }
      }
      
      return new ValueIndexCount(valueIndexCount);
   }
   
}
