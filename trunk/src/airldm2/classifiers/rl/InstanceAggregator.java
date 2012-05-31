package airldm2.classifiers.rl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import airldm2.classifiers.rl.estimator.AttributeEstimator;
import airldm2.classifiers.rl.estimator.AttributeValue;
import airldm2.classifiers.rl.estimator.Category;
import airldm2.classifiers.rl.estimator.Histogram;
import airldm2.classifiers.rl.estimator.Null;
import airldm2.classifiers.rl.estimator.Numeric;
import airldm2.classifiers.rl.estimator.OntologyAttributeEstimator;
import airldm2.classifiers.rl.estimator.SetAttributeEstimator;
import airldm2.classifiers.rl.estimator.SetAttributeValue;
import airldm2.classifiers.rl.ontology.Cut;
import airldm2.classifiers.rl.ontology.GlobalCut;
import airldm2.classifiers.rl.ontology.TBox;
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
   
   private static GlobalCut GlobalCut;

   public static AggregatedInstances init(LDInstances instances) throws RDFDatabaseException {
      RDFDataSource dataSource = (RDFDataSource) instances.getDataSource();
      RDFDataDescriptor dataDesc = (RDFDataDescriptor) instances.getDesc();
      
      List<URI> instanceURIs = dataSource.getTargetInstances(dataDesc.getTargetType());
      instanceURIs = sample(instanceURIs, 100);

      AggregatedInstances ais = new AggregatedInstances(instanceURIs);
      
      List<AggregatedInstance> aggInstances = CollectionUtil.makeList();
      for (URI instanceURI : instanceURIs) {
         RbcAttribute targetAttribute = dataDesc.getTargetAttribute();
         
         Category targetCategory = (Category) aggregateAttribute(dataSource, instanceURI, targetAttribute, null);
         
         AggregatedInstance aggInstance = new AggregatedInstance(instanceURI, targetCategory);
         aggInstances.add(aggInstance);
      }
      ais.setInstances(aggInstances);
      return ais;
   }

   private static List<URI> sample(List<URI> instanceURIs, int size) {
      List<URI> list = CollectionUtil.makeList();
      Collections.shuffle(instanceURIs, new Random(0));
      for (int i = 0; list.size() < size; i++) {
         if ('n' == instanceURIs.get(i).toString().charAt("http://ehr/stat/disease_pair/".length())) {
            list.add(instanceURIs.get(i));
         }
      }
      
      for (int i = 0; list.size() < size * 2; i++) {
         if ('n' != instanceURIs.get(i).toString().charAt("http://ehr/stat/disease_pair/".length())) {
            list.add(instanceURIs.get(i));
         }
      }
      return list;
   }

   public static AggregatedInstances aggregateAll(LDInstances instances, GlobalCut globalCut, Map<RbcAttribute, OntologyAttributeEstimator> estimators) throws RDFDatabaseException {
      GlobalCut = globalCut;
      AggregatedInstances ais = init(instances);
      
      RDFDataSource dataSource = (RDFDataSource) instances.getDataSource();
      RDFDataDescriptor dataDesc = (RDFDataDescriptor) instances.getDesc();
      List<RbcAttribute> nonTargetAttributes = dataDesc.getNonTargetAttributeList();
      
      for (RbcAttribute att : nonTargetAttributes) {
         OntologyAttributeEstimator est = estimators.get(att);
         List<AttributeValue> values = aggregateAttributeForInstances(dataSource, ais.getURIs(), att, est);
         ais.addAttribute(att, values);
      }
            
      return ais;
   }
   
   public static AggregatedInstances aggregateAll(LDInstances instances) throws RDFDatabaseException {
      return aggregateAll(instances, null, null);
   }

   public static List<AttributeValue> aggregateAttributeForInstances(RDFDataSource dataSource, List<URI> instances, RbcAttribute att, OntologyAttributeEstimator est) throws RDFDatabaseException {
      List<AttributeValue> values = CollectionUtil.makeList();
      for (URI instanceURI : instances) {
         try {
            AttributeValue value = aggregateAttribute(dataSource, instanceURI, att, est);
            values.add(value);
         } catch (IllegalArgumentException ex) {
            System.err.print(".");
            continue;
         }
      }
      
      return values;
   }

   private static AttributeValue aggregateSingleAttribute(RDFDataSource dataSource, URI instance, RbcAttribute attribute) throws RDFDatabaseException {
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
   
   private static AttributeValue aggregateAttribute(RDFDataSource dataSource, URI instance, RbcAttribute attribute, OntologyAttributeEstimator ontEst) throws RDFDatabaseException {
      if (attribute.getHierarchyRoot() == null || attribute.isHierarchicalHistogram()) {
         return aggregateSingleAttribute(dataSource, instance, attribute);
      } else if (attribute.isCutSum()) {
         TBox tBox = dataSource.getTBox();
         double sum = 0.0;
         Cut cut = GlobalCut.getCut(attribute);
         for (URI c : cut.get()) {
            RbcAttribute extendedAtt = attribute.extendWithHierarchy(c, tBox.isLeaf(c));
            Value value = dataSource.getValue(instance, extendedAtt);
            if (value != null) {
               sum += ((Literal)value).doubleValue();
            }
         }
         
         return new Numeric(sum);
      } else {
         SetAttributeEstimator setEst = (SetAttributeEstimator) ontEst;
         SetAttributeValue valueSet = new SetAttributeValue();
         for (AttributeEstimator est : setEst.getEstimatorSelection()) {
            RbcAttribute att = est.getAttribute();
            AttributeValue extendedAttValue = aggregateSingleAttribute(dataSource, instance, att);
            valueSet.add(att.getExtendedHierarchy(), extendedAttValue);
         }
         return valueSet;
      }
   }
   
}
