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

   public static List<AggregatedInstance> aggregate(LDInstances instances) throws RDFDatabaseException {
      RDFDataSource dataSource = (RDFDataSource) instances.getDataSource();
      RDFDataDescriptor dataDesc = (RDFDataDescriptor) instances.getDesc();
      
      List<URI> instanceURIs = dataSource.getTargetInstances(dataDesc.getTargetType());
      List<AggregatedInstance> aggInstances = CollectionUtil.makeList();
      for (URI instanceURI : instanceURIs) {
         RbcAttribute targetAttribute = dataDesc.getTargetAttribute();
         List<RbcAttribute> nonTargetAttributes = dataDesc.getAttributeList();
         
         int[] targetValueIndexCount = aggregateAttribute(dataSource, instanceURI, targetAttribute);
         int[][] featureValueIndexCount = new int[nonTargetAttributes.size()][];
         for (int i = 0; i < nonTargetAttributes.size(); i++) {
            featureValueIndexCount[i] = aggregateAttribute(dataSource, instanceURI, nonTargetAttributes.get(i));
         }
         
         AggregatedInstance aggInstance = new AggregatedInstance(featureValueIndexCount, targetValueIndexCount);
         aggInstances.add(aggInstance);
      }
      
      return aggInstances;      
   }

   private static int[] aggregateAttribute(RDFDataSource dataSource, URI instance, RbcAttribute attribute) throws RDFDatabaseException {
      ValueType valueType = attribute.getValueType();
      int[] valueIndexCount = new int[valueType.domainSize()];
      
      if (attribute.getAggregatorType() == ValueAggregator.INDEPENDENT_VAL) {
         for (int i = 0; i < valueIndexCount.length; i++) {
            valueIndexCount[i] = dataSource.countIndependentValueAggregation(instance, attribute, i);
         }
         
      } else if (attribute.getAggregatorType() == ValueAggregator.NONE) {
         Value value = dataSource.getValue(instance, attribute);
         int index = valueType.indexOf(value);
         if (index >= 0) {
            valueIndexCount[index] = 1;
         }
         
      } else {
         Value aggregatedValue = dataSource.getAggregation(instance, attribute);
         int index = valueType.indexOf(aggregatedValue);
         if (index >= 0) {
            valueIndexCount[index] = 1;
         }
      }
      
      return valueIndexCount;
   }
   
}
