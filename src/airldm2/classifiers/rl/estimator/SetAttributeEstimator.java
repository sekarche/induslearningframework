package airldm2.classifiers.rl.estimator;

import java.util.Map;

import org.openrdf.model.URI;

import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.util.CollectionUtil;

public class SetAttributeEstimator extends OntologyAttributeEstimator {

   private Map<URI,AttributeEstimator> mEstimators;
   
   public SetAttributeEstimator(RbcAttribute att) {
      super(att);
      mEstimators = CollectionUtil.makeMap();
   }

   @Override
   public void estimateParameters(RDFDataSource source, RDFDataDescriptor desc, ClassEstimator classEst) throws RDFDatabaseException {
      for (URI uri : mCut.get()) {
         AttributeEstimator est = mEstimators.get(uri);
         if (est == null) {
            RbcAttribute extendedAtt = mAttribute.extendWithHierarchy(uri);
            est = extendedAtt.getEstimator();
            mEstimators.put(uri, est);
         }
         est.estimateParameters(source, desc, classEst);
      }
   }

   @Override
   public double computeLikelihood(int classIndex, AttributeValue v) {
      double result = 1.0;
      for (URI uri : mCut.get()) {
         AttributeEstimator est = mEstimators.get(uri);
         double likelihood = est.computeLikelihood(classIndex, v);
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

}
