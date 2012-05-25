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
import airldm2.util.CollectionUtil;

public class SetAttributeEstimator extends OntologyAttributeEstimator {

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
            
            for (URI sub : subclasses) {
               AttributeEstimator subEst = mEstimators.get(sub);
               est.mergeWith(subEst);
            }
            mEstimators.put(sup, est);
         }
      }
      setCut(oldCut);
   }

   @Override
   public double computeLikelihood(int classIndex, AttributeValue v) {
      if (!(v instanceof SetAttributeValue)) 
         throw new IllegalArgumentException("Error: value " + v + " is not a SetAttributeValue for SetAttributeEstimator.");
      
      SetAttributeValue values = (SetAttributeValue) v;
      
      double result = 1.0;
      for (URI uri : mCut.get()) {
         AttributeEstimator est = mEstimators.get(uri);
         AttributeValue value = values.get(uri);
         double likelihood = est.computeLikelihood(classIndex, value);
         result *= likelihood;
      }
      return result;
   }

   @Override
   public double computeLL() {
      double result = 0.0;
      for (URI uri : mCut.get()) {
         AttributeEstimator est = mEstimators.get(uri);
         result += est.computeLL();
      }
      return result;
   }

   @Override
   public double computeDualLL() {
      double result = 0.0;
      for (URI uri : mCut.get()) {
         AttributeEstimator est = mEstimators.get(uri);
         result += est.computeDualLL();
      }
      return result;
   }
   
   @Override
   public String toString() {
      return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
         .append("name", mAttribute.getName())
         .append("mEstimators", mEstimators)
         .toString();
   }

}
