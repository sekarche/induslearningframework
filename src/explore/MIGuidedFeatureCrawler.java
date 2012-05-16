package explore;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.openrdf.model.URI;

import airldm2.core.ISufficentStatistic;
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
import airldm2.database.rdf.SuffStatQueryParameter;
import airldm2.exceptions.RDFDataDescriptorFormatException;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.util.ArrayUtil;
import airldm2.util.CollectionUtil;
import explore.database.rdf.NestedAggregationQueryConstructor.Aggregator;
import explore.database.rdf.RangeTypeQueryConstructor.RangeType;
import explore.mitree.OpenNodeVisitor;
import explore.mitree.PropertyTree;
import explore.mitree.RbcAttributeScore;
import explore.mitree.TreeNode;

public class MIGuidedFeatureCrawler {

   private static final int MAX_PROPERTY_RANGE_FOR_FEATURE = 10;
   private static final double SCORE_THRESHOLD = 0.005;
   
   private RDFDataSource mDataSource;
   private RDFDataDescriptor cDesc;
   private URI[] mExclusion;
   private PropertyTree mTree;
   
   public MIGuidedFeatureCrawler(RDFDataSource dataSource, String inDescFile, OpenNodeVisitor expansionStrategy) throws IOException, RDFDataDescriptorFormatException {
      mDataSource = dataSource;
      cDesc = RDFDataDescriptorParser.parse(inDescFile);
      mTree = new PropertyTree(expansionStrategy);
   }
   
   public void setExclusion(URI[] exclusion) {
      mExclusion = exclusion;
   }

   public void crawl(String outDescFile, int crawlSize, int featureSize) throws IOException, RDFDataDescriptorFormatException, RDFDatabaseException {
      crawl(crawlSize, featureSize);
      BufferedWriter out = new BufferedWriter(new FileWriter(outDescFile));
      cDesc.write(out);
      out.close();
   }
   
   public void crawl(int crawlSize, int featureSize) throws RDFDatabaseException, IOException, RDFDataDescriptorFormatException {
      while (cDesc.getNonTargetAttributeCount() < crawlSize) {
         TreeNode n = mTree.getNextNodeToExpand();
         if (n == null) break;
         
         RbcAttributeScore attribute = makeBestAttribute(n.getPropertyChain());
         if (attribute != null) {
            cDesc.addNonTargetAttribute(attribute.Attribute);
         }
         
         List<PropertyChain> childrenProp = crawlChildren(n.getPropertyChain());
         mTree.expand(n, attribute, childrenProp);
      }
      //tree.print();
   }
   
   private List<PropertyChain> crawlChildren(PropertyChain propChain) throws RDFDatabaseException {
      final PropertyChain TARGET_CHAIN = cDesc.getTargetAttribute().getPropertyChain();
      
      List<URI> props = mDataSource.getPropertiesOf(cDesc.getTargetType(), propChain);
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
   
   private RbcAttributeScore makeBestAttribute(PropertyChain propChain) throws RDFDatabaseException {
      if (propChain == null) return null;
      
      List<RbcAttribute> attributes = makeAttributes(propChain);
      if (attributes.isEmpty()) return null;
      
      List<RbcAttributeScore> as = calculateScore(attributes);
      return Collections.max(as);
   }
   
   private List<RbcAttribute> makeAttributes(PropertyChain propChain) throws RDFDatabaseException {
      List<RbcAttribute> allAttributes = CollectionUtil.makeList();
      
      if (propChain.isID()) return allAttributes;
      
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
         int rangeSize = mDataSource.getRangeSizeOf(cDesc.getTargetType(), propChain);
         if (rangeSize <= MAX_PROPERTY_RANGE_FOR_FEATURE) {
            SPARQLQueryResult result = mDataSource.getRangeOf(cDesc.getTargetType(), propChain);
            
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
      
      if (!isUnique) {
         double average = mDataSource.getAverageForAggregation(cDesc.getTargetType(), propChain, Aggregator.COUNT);
         ValueType valueType = new BinnedType(new double[] { average });
         ValueAggregator valueAgg = ValueAggregator.COUNT;
         allAttributes.add(new RbcAttribute(propChain.toString() + index++, propChain, valueType, valueAgg));
      }
      
      return allAttributes;
   }
   
   private List<RbcAttributeScore> calculateScore(List<RbcAttribute> att) throws RDFDatabaseException {
      List<RbcAttributeScore> scores = CollectionUtil.makeList();
      for (RbcAttribute a : att) {
         double score = calculateScore(a);
         scores.add(new RbcAttributeScore(a, score));
      }
      return scores;
   }

   private double calculateScore(RbcAttribute att) throws RDFDatabaseException {
      RbcAttribute targetAttribute = cDesc.getTargetAttribute();
      
      //[class value][attribute value]
      double[][] counts = new double[targetAttribute.getDomainSize()][att.getDomainSize()];
      for (int c = 0; c < counts.length; c++) {
         for (int a = 0; a < counts[0].length; a++) {
            SuffStatQueryParameter queryParam = new SuffStatQueryParameter(cDesc.getTargetType(), targetAttribute, c, att, a);
            ISufficentStatistic tempSuffStat = mDataSource.getSufficientStatistic(queryParam);
            counts[c][a] += tempSuffStat.getValue().intValue();
         }
      }
      
      return calculateKLScore(counts);
   }

   private double calculateKLScore(double[][] counts) {
      //counts[class value][attribute value]
      
      //Smoothing
      ArrayUtil.add(counts, 1.0);
      
      double[][] probs = ArrayUtil.normalize(counts);
      
      double score = 0.0;
      double[] attributeProb = ArrayUtil.sumDimension(probs, 1);
      double[] classProb = ArrayUtil.sumDimension(probs, 2);
      final double LOG2 = Math.log(2.0);
      for (int c = 0; c < counts.length; c++) {
         for (int a = 0; a < counts[0].length; a++) {
            score += probs[c][a] * Math.log(probs[c][a] / (classProb[c] * attributeProb[a])) / LOG2; 
         }
      }
      
      return score;
   }

   private void filterLowScores(List<RbcAttributeScore> ac) {
      for (Iterator<RbcAttributeScore> it = ac.iterator(); it.hasNext(); ) {
         RbcAttributeScore next = it.next();
         if (next.Score < SCORE_THRESHOLD) {
            it.remove();
         }
      }
   }
  
}
