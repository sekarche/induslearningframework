package airldm2.classifiers.rl.estimator;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openrdf.model.URI;

import airldm2.classifiers.rl.ontology.Cut;
import airldm2.classifiers.rl.ontology.TBox;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.util.MathUtil;
import airldm2.util.CollectionUtil;

public class SetAttributeEstimator extends OntologyAttributeEstimator {

   public enum SetSelection { ALL, BEST, BEST_AND_ANCESTORS };
   public enum SetAggregation { SUM, AVERAGE };
   
   public static SetSelection Selection = SetSelection.ALL;
   public static SetAggregation Aggregation = SetAggregation.AVERAGE;
   
   private Map<URI,AttributeEstimator> mEstimators;
   
   public SetAttributeEstimator(TBox tBox, RbcAttribute att) {
      super(tBox, att);
      mEstimators = CollectionUtil.makeMap();
   }

   @Override
   public void estimateParameters(RDFDataSource source, RDFDataDescriptor desc, ClassEstimator classEst) throws RDFDatabaseException {
      for (URI uri : mCut.get()) {
         AttributeEstimator est = mEstimators.get(uri);
         if (est == null) {
            RbcAttribute extendedAtt = mAttribute.extendWithHierarchy(uri, mTBox.isLeaf(uri));
            est = extendedAtt.getEstimator();
            mEstimators.put(uri, est);
            est.estimateParameters(source, desc, classEst);
         }
      }
   }

   @Override
   public void estimateAllParameters(RDFDataSource source, RDFDataDescriptor desc, ClassEstimator classEst) throws RDFDatabaseException {
      URI hierarchyRoot = mAttribute.getHierarchyRoot();
      Cut cut = mTBox.getLeafCut(hierarchyRoot);
      Cut oldCut = mCut;
      setCut(cut);
      estimateParameters(source, desc, classEst);
      
      while ((cut = cut.abstractCut()) != null) {
         for (URI sup : cut.get()) {
            if (mEstimators.containsKey(sup)) continue;
            
            RbcAttribute extendedAtt = mAttribute.extendWithHierarchy(sup, mTBox.isLeaf(sup));
            AttributeEstimator est = extendedAtt.getEstimator();
            List<URI> subclasses = mTBox.getDirectSubclass(sup);
            List<AttributeEstimator> subEstimators = CollectionUtil.makeList();
            for (URI sub : subclasses) {
               AttributeEstimator subEst = mEstimators.get(sub);
               subEstimators.add(subEst);
            }
            est.mergeWith(subEstimators);
            
            mEstimators.put(sup, est);
         }
      }
      setCut(oldCut);
   }

   public List<AttributeEstimator> getEstimatorSelection() {
      List<AttributeEstimator> selection = CollectionUtil.makeList();
      if (Selection == SetSelection.ALL) {
         for (URI uri : mCut.get()) {
            AttributeEstimator est = mEstimators.get(uri);
            if (!est.isValid()) continue;
            selection.add(est);
         }
      } else if (Selection == SetSelection.BEST_AND_ANCESTORS) {
         AttributeEstimator current = getBestEstimator();
         if (current != null) {
            URI currentURI = current.getAttribute().getExtendedHierarchy();
            selection.add(current);
            while (true) {
               Log.warning(current.getAttribute().getExtendedHierarchy() + " " + current.toString());
               URI sup = mTBox.getDirectSuperclass(currentURI);
               AttributeEstimator estSup = mEstimators.get(sup);
               if (sup == null || estSup == null) break;
               
               if (estSup.isValid()) {
                  selection.add(estSup);
               }
               current = estSup;
               currentURI = sup; 
            }
         }
      } else {
         AttributeEstimator best = getBestEstimator();
         if (best != null) {
            selection.add(best);
         }
      }
      
      return selection;
   }
      
   public AttributeEstimator getBestEstimator() {
      AttributeEstimator bestEst = null;
      double bestScore = Double.NEGATIVE_INFINITY;
      for (URI uri : mCut.get()) {
         AttributeEstimator est = mEstimators.get(uri);
         if (!est.isValid()) continue;
         
         double score = est.score();
         if (score > bestScore) {
            bestEst = est;
            bestScore = score;
         }
      }
      return bestEst;
   }
   
   @Override
   public boolean isValid() {
      for (AttributeEstimator est : getEstimatorSelection()) {
         if (est.isValid()) return true;
      }
      return false;
   }
   
   @Override
   public double computeLikelihood(int classIndex, AttributeValue v) {
      if (!(v instanceof SetAttributeValue)) 
         throw new IllegalArgumentException("Error: value " + v + " is not a SetAttributeValue for SetAttributeEstimator.");
      
      SetAttributeValue values = (SetAttributeValue) v;
      
      List<Double> result = CollectionUtil.makeList();
      for (AttributeEstimator est : getEstimatorSelection()) {
         URI uri = est.getAttribute().getExtendedHierarchy();
         AttributeValue value = values.get(uri);
         
         double likelihood = est.computeLikelihood(classIndex, value);
         //System.out.print(uri + " v=" + value + "LL=" + likelihood);
         result.add(likelihood);
      }

      if (result.isEmpty()) return 0.0;
      if (Aggregation == SetAggregation.SUM) {
         return MathUtil.sum(result);
      } else if (Aggregation == SetAggregation.AVERAGE) {
         return MathUtil.averageLog(result);
      }
      
      throw new RuntimeException();
   }

   @Override
   public double computeLL() {
      List<Double> result = CollectionUtil.makeList();
      for (AttributeEstimator est : getEstimatorSelection()) {
         result.add(est.computeLL());
      }
      
      if (result.isEmpty()) return -10000;
      if (Aggregation == SetAggregation.SUM) {
         return MathUtil.sum(result);
      } else if (Aggregation == SetAggregation.AVERAGE) {
         return MathUtil.averageLog(result);
      }

      throw new RuntimeException();
   }

   @Override
   public double computeDualLL() {
      List<Double> result = CollectionUtil.makeList();
      for (AttributeEstimator est : getEstimatorSelection()) {
         result.add(est.computeDualLL());
      }
      
      if (result.isEmpty()) return -10000;
      if (Aggregation == SetAggregation.SUM) {
         return MathUtil.sum(result);
      } else if (Aggregation == SetAggregation.AVERAGE) {
         return MathUtil.averageLog(result);
      }

      throw new RuntimeException();
   }
   
   @Override
   public String toString() {
      return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
         .append("name", mAttribute.getName())
         .append("mEstimators", getEstimatorSelection())         
         .toString();
   }

   @Override
   public double paramSize() {
      double size = 0.0;
      for (AttributeEstimator est : getEstimatorSelection()) {
         size += est.paramSize();
      }
      return size;
   }

}
