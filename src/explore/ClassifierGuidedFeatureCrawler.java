package explore;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

import org.openrdf.model.URI;

import weka.classifiers.evaluation.ConfusionMatrix;
import weka.core.Matrix;
import airldm2.classifiers.Evaluation;
import airldm2.classifiers.rl.AggregatedInstances;
import airldm2.classifiers.rl.InstanceAggregator;
import airldm2.classifiers.rl.RelationalBayesianClassifier;
import airldm2.classifiers.rl.estimator.AttributeEstimator;
import airldm2.classifiers.rl.estimator.Histogram;
import airldm2.core.LDInstances;
import airldm2.core.rl.BinnedType;
import airldm2.core.rl.EnumType;
import airldm2.core.rl.NominalType;
import airldm2.core.rl.PropertyChain;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataDescriptorParser;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.core.rl.ValueAggregator;
import airldm2.core.rl.ValueType;
import airldm2.database.rdf.SPARQLQueryResult;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.util.CollectionUtil;
import explore.database.rdf.NestedAggregationQueryConstructor.Aggregator;
import explore.database.rdf.RangeTypeQueryConstructor.RangeType;
import explore.rbctree.PropertyTree;
import explore.rbctree.TreeNode;
import explore.rbctree.TreeVisitor;

public class ClassifierGuidedFeatureCrawler {

   private static final int MAX_PROPERTY_RANGE_FOR_FEATURE = 10;
   
   private final RDFDataSource mSubtrainData;
   private final RDFDataSource mTuneData;
   private final LDInstances mSubtrainInstances;
   private final LDInstances mTuneInstances;
   
   private RDFDataDescriptor cDesc;
   private RelationalBayesianClassifier cRBC;
   private RelationalBayesianClassifier cSubRBC;
   private RelationalBayesianClassifier cSubRBC2;
   private AggregatedInstances cTuneAggInstances;
   private AggregatedInstances cTuneAggInstances2;
   private PropertyTree cPropertyTree;
   private URI[] mExclusion;
   
   public ClassifierGuidedFeatureCrawler(RDFDataSource trainData, RDFDataSource subtrainData, RDFDataSource tuneData, String inDescFile) throws Exception {
      mSubtrainData = subtrainData;
      mTuneData = tuneData;
      LDInstances trainInstances = new LDInstances();
      trainInstances.setDataSource(trainData);
      mSubtrainInstances = new LDInstances();
      mSubtrainInstances.setDataSource(subtrainData);
      mTuneInstances = new LDInstances();
      mTuneInstances.setDataSource(mTuneData);
      cDesc = RDFDataDescriptorParser.parse(inDescFile);
      trainInstances.setDesc(cDesc);
      mSubtrainInstances.setDesc(cDesc);
      mTuneInstances.setDesc(cDesc);
      //Empty classifier to initialize data source and descriptor
      cRBC = new RelationalBayesianClassifier();
      cRBC.buildClassifier(trainInstances);
      cSubRBC = new RelationalBayesianClassifier();
      cSubRBC.buildClassifier(mSubtrainInstances);
      cSubRBC2 = new RelationalBayesianClassifier();
      cSubRBC2.buildClassifier(mTuneInstances);
      cTuneAggInstances = InstanceAggregator.init(mTuneInstances);
      cTuneAggInstances2 = InstanceAggregator.init(mSubtrainInstances);
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
      while (cDesc.getNonTargetAttributeCount() < crawlSize) {
         TreeNode n = cPropertyTree.getNextNodeToExpand();
         if (n == null) break;
         
         RbcAttribute att = n.getAttribute();
         if (att != null) {
            cDesc.addNonTargetAttribute(att);
            cSubRBC.addAttributeCounts(n.getRBCCount());
            cTuneAggInstances.addAttribute(n.getValueIndexCountForTuneInstances());
            cSubRBC2.addAttributeCounts(n.getRBCCount2());
            cTuneAggInstances2.addAttribute(n.getValueIndexCountForTuneInstances2());
            
            AttributeEstimator counts = cRBC.getCounts(att);
            cRBC.addAttributeCounts(counts);
         }
         
         List<PropertyChain> childrenProp = crawlChildren(n.getPropertyChain());
         List<RbcAttribute> childrenAtt = makeAttributes(childrenProp);
         List<AttributeEstimator> childrenRBCCounts = makeRBCCounts(childrenAtt);
         List<List<Histogram>> valueIndexCountForAttributes = makeAggregateAttributeForTuneData(childrenAtt);
         List<AttributeEstimator> childrenRBCCounts2 = makeRBCCounts2(childrenAtt);
         List<List<Histogram>> valueIndexCountForAttributes2 = makeAggregateAttributeForTuneData2(childrenAtt);
         
         cPropertyTree.expand(n, childrenAtt, childrenRBCCounts, childrenRBCCounts2, valueIndexCountForAttributes, valueIndexCountForAttributes2);

         //tree.print();
         if (childrenAtt.isEmpty()) continue;
         cPropertyTree.accept(new TreeVisitor() {

            @Override
            public void visit(TreeNode node) {
               if (!node.isOpen()) return;
               
               cSubRBC.addAttributeCounts(node.getRBCCount());
               List<Histogram> valueIndexCountForTuneInstances = node.getValueIndexCountForTuneInstances();
               cTuneAggInstances.addAttribute(valueIndexCountForTuneInstances);

               cSubRBC2.addAttributeCounts(node.getRBCCount2());
               List<Histogram> valueIndexCountForTuneInstances2 = node.getValueIndexCountForTuneInstances2();
               cTuneAggInstances2.addAttribute(valueIndexCountForTuneInstances2);

               
               double score = 0.0;
               try {
                  ConfusionMatrix matrix = Evaluation.evaluateBuiltRBCModel(cSubRBC, cDesc, cTuneAggInstances);
                  ConfusionMatrix matrix2 = Evaluation.evaluateBuiltRBCModel(cSubRBC2, cDesc, cTuneAggInstances2);
                  score = 2.0 - matrix.errorRate() - matrix2.errorRate();
               } catch (Exception e) {
                  e.printStackTrace();
               }
               
               node.setScore(score);
               System.out.println(score + ": " + node.getPropertyChain());
               
               cSubRBC.removeLastAttributeCounts();
               cTuneAggInstances.removeLastAttribute();
               
               cSubRBC2.removeLastAttributeCounts();
               cTuneAggInstances2.removeLastAttribute();
            }
            
         });
      }
      //tree.print();
      
      return cRBC;
   }
   
   private List<PropertyChain> crawlChildren(PropertyChain propChain) throws RDFDatabaseException {
      final PropertyChain TARGET_CHAIN = cDesc.getTargetAttribute().getPropertyChain();
      
      List<URI> props = mSubtrainData.getPropertiesOf(cDesc.getTargetType(), propChain);
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
      RangeType rangeType = mSubtrainData.getRangeTypeOf(cDesc.getTargetType(), propChain);
      boolean isUnique = mSubtrainData.isUniqueForInstance(cDesc.getTargetType(), propChain);
      
      if (rangeType == RangeType.NUMERIC) {
         double average = 0.0;
         ValueType valueType = null;
         ValueAggregator valueAgg = null;
         
         average = mSubtrainData.getAverageForAggregation(cDesc.getTargetType(), propChain, Aggregator.AVG);
         valueType = new BinnedType(new double[] { average });
         valueAgg = ValueAggregator.AVG;
         allAttributes.add(new RbcAttribute(propChain.toString() + index++, propChain, valueType, valueAgg));
         
         if (!isUnique) {
            average = mSubtrainData.getAverageForAggregation(cDesc.getTargetType(), propChain, Aggregator.MIN);
            valueType = new BinnedType(new double[] { average });
            valueAgg = ValueAggregator.MIN;
            allAttributes.add(new RbcAttribute(propChain.toString() + index++, propChain, valueType, valueAgg));
            
            average = mSubtrainData.getAverageForAggregation(cDesc.getTargetType(), propChain, Aggregator.MAX);
            valueType = new BinnedType(new double[] { average });
            valueAgg = ValueAggregator.MAX;
            allAttributes.add(new RbcAttribute(propChain.toString() + index++, propChain, valueType, valueAgg));
         }
         
      } else {
         int rangeSize = mSubtrainData.getRangeSizeOf(cDesc.getTargetType(), propChain);
         if (rangeSize <= MAX_PROPERTY_RANGE_FOR_FEATURE) {
            SPARQLQueryResult result = mSubtrainData.getRangeOf(cDesc.getTargetType(), propChain);
            
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
                     ValueAggregator valueAgg = ValueAggregator.HISTOGRAM;
                     allAttributes.add(new RbcAttribute(propChain.toString() + index++, propChain, valueType, valueAgg));
                  }
               } else if (rangeType == RangeType.STRING) {
                  List<String> resultList = result.getStringList();
                  if (resultList.size() >= 2) {
                     ValueType valueType = new NominalType(resultList);
                     ValueAggregator valueAgg = ValueAggregator.HISTOGRAM;
                     allAttributes.add(new RbcAttribute(propChain.toString() + index++, propChain, valueType, valueAgg));
                  }
               }
            }
         }
      }
      
//      if (!isUnique) {
//         double average = mSubtrainData.getAverageForAggregation(cDesc.getTargetType(), propChain, Aggregator.COUNT);
//         ValueType valueType = new BinnedType(new double[] { average });
//         ValueAggregator valueAgg = ValueAggregator.COUNT;
//         allAttributes.add(new RbcAttribute(propChain.toString() + index++, propChain, valueType, valueAgg));
//      }
      
      return allAttributes;
   }
   
   private List<AttributeEstimator> makeRBCCounts(List<RbcAttribute> childrenAtt) throws RDFDatabaseException {
      List<AttributeEstimator> allCounts = CollectionUtil.makeList();
      for (RbcAttribute att : childrenAtt) {
         AttributeEstimator counts = cSubRBC.getCounts(att);
         allCounts.add(counts);
      }
      return allCounts;
   }

   private List<List<Histogram>> makeAggregateAttributeForTuneData(List<RbcAttribute> childrenAtt) throws RDFDatabaseException {
      List<List<Histogram>> indexCountForAttribute = CollectionUtil.makeList();
      for (RbcAttribute att : childrenAtt) {
         List<Histogram> indexCounts = InstanceAggregator.aggregateAttributeForInstances(mTuneData, cTuneAggInstances.getURIs(), att);
         indexCountForAttribute.add(indexCounts);
         System.out.println(att.getPropertyChain());
      }
      return indexCountForAttribute;
   }

   private List<AttributeEstimator> makeRBCCounts2(List<RbcAttribute> childrenAtt) throws RDFDatabaseException {
      List<AttributeEstimator> allCounts = CollectionUtil.makeList();
      for (RbcAttribute att : childrenAtt) {
         AttributeEstimator counts = cSubRBC2.getCounts(att);
         allCounts.add(counts);
      }
      return allCounts;
   }

   private List<List<Histogram>> makeAggregateAttributeForTuneData2(List<RbcAttribute> childrenAtt) throws RDFDatabaseException {
      List<List<Histogram>> indexCountForAttribute = CollectionUtil.makeList();
      for (RbcAttribute att : childrenAtt) {
         List<Histogram> indexCounts = InstanceAggregator.aggregateAttributeForInstances(mSubtrainData, cTuneAggInstances2.getURIs(), att);
         indexCountForAttribute.add(indexCounts);
         System.out.println(att.getPropertyChain());
      }
      return indexCountForAttribute;
   }

}
