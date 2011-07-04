package explore;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.openrdf.model.URI;

import weka.classifiers.evaluation.ConfusionMatrix;
import airldm2.classifiers.Evaluation;
import airldm2.classifiers.rl.AggregatedInstances;
import airldm2.classifiers.rl.ClassValueCount;
import airldm2.classifiers.rl.InstanceAggregator;
import airldm2.classifiers.rl.RelationalBayesianClassifier;
import airldm2.classifiers.rl.ValueIndexCount;
import airldm2.core.LDInstances;
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
import explore.rbctree.PropertyTree;
import explore.rbctree.TreeNode;
import explore.rbctree.TreeVisitor;

public class ClassifierGuidedFeatureCrawler {

   private static final int MAX_PROPERTY_RANGE_FOR_FEATURE = 10;
   
   private final RDFDataSource mTrainData;
   private final RDFDataSource mTuneData;
   private final LDInstances mTrainInstances;
   private final LDInstances mTuneInstances;
   
   private RDFDataDescriptor cDesc;
   private RelationalBayesianClassifier cRBC;
   private AggregatedInstances cTuneAggInstances;
   private PropertyTree cPropertyTree;
   private URI[] mExclusion;

   
   public ClassifierGuidedFeatureCrawler(RDFDataSource trainData, RDFDataSource tuneData, String inDescFile) throws Exception {
      mTrainData = trainData;
      mTuneData = tuneData;
      mTrainInstances = new LDInstances();
      mTrainInstances.setDataSource(trainData);
      mTuneInstances = new LDInstances();
      mTuneInstances.setDataSource(mTuneData);
      cDesc = RDFDataDescriptorParser.parse(inDescFile);
      mTrainInstances.setDesc(cDesc);
      mTuneInstances.setDesc(cDesc);
      cRBC = new RelationalBayesianClassifier();
      //Empty classifier to initialize data source and descriptor
      cRBC.buildClassifier(mTrainInstances);
      cTuneAggInstances = InstanceAggregator.init(mTuneInstances);
      cPropertyTree = new PropertyTree();
   }

   public void setExclusion(URI[] exclusion) {
      mExclusion = exclusion;
   }

   public RelationalBayesianClassifier crawlAndWriteDesc(String outDescFile, int crawlSize) throws Exception {
      RelationalBayesianClassifier rbc = crawl(crawlSize);
      BufferedWriter out = new BufferedWriter(new FileWriter(outDescFile));
      cDesc.write(out);
      out.close();
      return rbc;
   }
   
   public RelationalBayesianClassifier crawl(int crawlSize) throws Exception {
      while (cPropertyTree.attributeSize() < crawlSize) {
         TreeNode n = cPropertyTree.getNextNodeToExpand();
         if (n == null) break;
         
         RbcAttribute att = n.getAttribute();
         if (att != null) {
            cDesc.addNonTargetAttribute(att);
            cRBC.addAttributeCounts(n.getRBCCount());
            cTuneAggInstances.addAttribute(n.getValueIndexCountForTuneInstances());
         }
         
         List<PropertyChain> childrenProp = crawlChildren(n.getPropertyChain());
         List<RbcAttribute> childrenAtt = makeAttributes(childrenProp);
         List<ClassValueCount> childrenRBCCounts = makeRBCCounts(childrenAtt);
         List<List<ValueIndexCount>> valueIndexCountForAttributes = makeAggregateAttributeForTuneData(childrenAtt);
         
         cPropertyTree.expand(n, childrenAtt, childrenRBCCounts, valueIndexCountForAttributes);

         //tree.print();
         if (childrenAtt.isEmpty()) continue;
         cPropertyTree.accept(new TreeVisitor() {

            @Override
            public void visit(TreeNode node) {
               if (!node.isOpen()) return;
               
               cRBC.addAttributeCounts(node.getRBCCount());
               List<ValueIndexCount> valueIndexCountForTuneInstances = node.getValueIndexCountForTuneInstances();
               cTuneAggInstances.addAttribute(valueIndexCountForTuneInstances);

               double score = 0.0;
               try {
                  ConfusionMatrix matrix = Evaluation.evaluateBuiltRBCModel(cRBC, cDesc, cTuneAggInstances);
                  score = 1.0 - matrix.errorRate();
               } catch (Exception e) {
                  e.printStackTrace();
               }
               
               node.setScore(score);
               System.out.println(score + ": " + node.getPropertyChain());
               
               cRBC.removeLastAttributeCounts();
               cTuneAggInstances.removeLastAttribute();
            }
            
         });
      }
      //tree.print();
      
      return cRBC;
   }
   
   private List<PropertyChain> crawlChildren(PropertyChain propChain) throws RDFDatabaseException {
      final PropertyChain TARGET_CHAIN = cDesc.getTargetAttribute().getPropertyChain();
      
      List<URI> props = mTrainData.getPropertiesOf(cDesc.getTargetType(), propChain);
      List<PropertyChain> children = CollectionUtil.makeList();
      for (URI prop : props) {
         PropertyChain newChain = PropertyChain.make(propChain, prop);
         if (newChain.contains(TARGET_CHAIN)
               || newChain.containsDuplicate()
               || newChain.hasURIStartsWith(mExclusion)) continue;
         
         children.add(newChain);
      }
      return children;
   }
   
   private List<RbcAttribute> makeAttributes(List<PropertyChain> propChains) throws RDFDatabaseException {
      List<RbcAttribute> allAttributes = CollectionUtil.makeList();
      for (PropertyChain p : propChains) {
         allAttributes.addAll(makeAttributes(p));
      }
      return allAttributes;
   }
   
   private List<RbcAttribute> makeAttributes(PropertyChain propChain) throws RDFDatabaseException {
      List<RbcAttribute> allAttributes = CollectionUtil.makeList();
      int index = 1;
      RangeType rangeType = mTrainData.getRangeTypeOf(cDesc.getTargetType(), propChain);
      boolean isUnique = mTrainData.isUniqueForInstance(cDesc.getTargetType(), propChain);
      
      if (rangeType == RangeType.NUMERIC) {
         double average = 0.0;
         ValueType valueType = null;
         ValueAggregator valueAgg = null;
         
         average = mTrainData.getAverageForAggregation(cDesc.getTargetType(), propChain, Aggregator.AVG);
         valueType = new BinnedType(new double[] { average });
         valueAgg = ValueAggregator.AVG;
         allAttributes.add(new RbcAttribute(propChain.toString() + index++, propChain, valueType, valueAgg));
         
         if (!isUnique) {
            average = mTrainData.getAverageForAggregation(cDesc.getTargetType(), propChain, Aggregator.MIN);
            valueType = new BinnedType(new double[] { average });
            valueAgg = ValueAggregator.MIN;
            allAttributes.add(new RbcAttribute(propChain.toString() + index++, propChain, valueType, valueAgg));
            
            average = mTrainData.getAverageForAggregation(cDesc.getTargetType(), propChain, Aggregator.MAX);
            valueType = new BinnedType(new double[] { average });
            valueAgg = ValueAggregator.MAX;
            allAttributes.add(new RbcAttribute(propChain.toString() + index++, propChain, valueType, valueAgg));
         }
         
      } else {
         int rangeSize = mTrainData.getRangeSizeOf(cDesc.getTargetType(), propChain);
         if (rangeSize <= MAX_PROPERTY_RANGE_FOR_FEATURE) {
            SPARQLQueryResult result = mTrainData.getRangeOf(cDesc.getTargetType(), propChain);
            
            if (isUnique) {
               if (rangeType == RangeType.IRI) {
                  List<URI> resultList = result.getURIList();
                  if (resultList.size() >= 2) {
                     ValueType valueType = new EnumType(resultList);
                     ValueAggregator valueAgg = ValueAggregator.NONE;
                     allAttributes.add(new RbcAttribute(propChain.toString() + index++, propChain, valueType, valueAgg));
                  }
               } else if (rangeType == RangeType.STRING) {
                  List<String> resultList = result.getStringList();
                  if (resultList.size() >= 2) {
                     ValueType valueType = new NominalType(resultList);
                     ValueAggregator valueAgg = ValueAggregator.NONE;
                     allAttributes.add(new RbcAttribute(propChain.toString() + index++, propChain, valueType, valueAgg));
                  }
               }
            } else {
               if (rangeType == RangeType.IRI) {
                  List<URI> resultList = result.getURIList();
                  if (resultList.size() >= 2) {
                     ValueType valueType = new EnumType(resultList);
                     ValueAggregator valueAgg = ValueAggregator.INDEPENDENT_VAL;
                     allAttributes.add(new RbcAttribute(propChain.toString() + index++, propChain, valueType, valueAgg));
                  }
               } else if (rangeType == RangeType.STRING) {
                  List<String> resultList = result.getStringList();
                  if (resultList.size() >= 2) {
                     ValueType valueType = new NominalType(resultList);
                     ValueAggregator valueAgg = ValueAggregator.INDEPENDENT_VAL;
                     allAttributes.add(new RbcAttribute(propChain.toString() + index++, propChain, valueType, valueAgg));
                  }
               }
            }
         }
      }
      
      if (!isUnique) {
         double average = mTrainData.getAverageForAggregation(cDesc.getTargetType(), propChain, Aggregator.COUNT);
         ValueType valueType = new BinnedType(new double[] { average });
         ValueAggregator valueAgg = ValueAggregator.COUNT;
         allAttributes.add(new RbcAttribute(propChain.toString() + index++, propChain, valueType, valueAgg));
      }
      
      return allAttributes;
   }
   
   private List<ClassValueCount> makeRBCCounts(List<RbcAttribute> childrenAtt) throws RDFDatabaseException {
      List<ClassValueCount> allCounts = CollectionUtil.makeList();
      for (RbcAttribute att : childrenAtt) {
         ClassValueCount counts = cRBC.getCounts(att);
         allCounts.add(counts);
      }
      return allCounts;
   }

   private List<List<ValueIndexCount>> makeAggregateAttributeForTuneData(List<RbcAttribute> childrenAtt) throws RDFDatabaseException {
      List<List<ValueIndexCount>> indexCountForAttribute = CollectionUtil.makeList();
      for (RbcAttribute att : childrenAtt) {
         List<ValueIndexCount> indexCounts = InstanceAggregator.aggregateAttributeForInstances(mTuneData, cTuneAggInstances.getURIs(), att);
         indexCountForAttribute.add(indexCounts);
         System.out.println(att.getPropertyChain());
      }
      return indexCountForAttribute;
   }

}
