package explore;

import java.util.Collections;
import java.util.List;

import explore.mitree.RbcAttributeScore;

import airldm2.core.ISufficentStatistic;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.database.rdf.SuffStatQueryParameter;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.util.ArrayUtil;
import airldm2.util.CollectionUtil;

public class RDFFeatureSelector {

   private RDFDataSource mDataSource;
   private RDFDataDescriptor cDesc;
   
   public RDFFeatureSelector(RDFDataSource dataSource) {
      mDataSource = dataSource;
   }

   public void select(RDFDataDescriptor desc, int featureSize) throws RDFDatabaseException {
      cDesc = desc;
      List<RbcAttribute> attributes = CollectionUtil.makeList(cDesc.getNonTargetAttributeList());
      cDesc.clearNonTargetAttributes();
      
      List<RbcAttributeScore> scores = CollectionUtil.makeList();
      for (RbcAttribute a : attributes) {
         double score = calculateScore(a);
         scores.add(new RbcAttributeScore(a, score));
      }
      
      //System.out.println(scores);
      Collections.sort(scores);
      Collections.reverse(scores);
      scores = scores.subList(0, Math.min(scores.size(), featureSize));
      
      attributes.clear();
      for (RbcAttributeScore score : scores) {
         attributes.add(score.Attribute);
      }
      
      System.out.println(attributes);
      cDesc.addNonTargetAttributes(attributes);
   }

   private double calculateScore(RbcAttribute att) throws RDFDatabaseException {
      RbcAttribute targetAttribute = cDesc.getTargetAttribute();
      
      //[class value][attribute value]
      double[][] counts = new double[targetAttribute.getDomainSize()][att.getDomainSize()];
      for (int c = 0; c < counts.length; c++) {
         for (int a = 0; a < counts[0].length; a++) {
            SuffStatQueryParameter queryParam = new SuffStatQueryParameter(cDesc.getTargetType(), targetAttribute, c, att, a);
            ISufficentStatistic tempSuffStat = mDataSource.getMultinomialSufficientStatistic(queryParam);
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

}
