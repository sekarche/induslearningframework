package airldm2.classifiers.rl.estimator;

import airldm2.classifiers.rl.ontology.TBox;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.exceptions.RDFDatabaseException;

public class SingleAttributeEstimator extends OntologyAttributeEstimator {

   private AttributeEstimator mEstimator;

   public SingleAttributeEstimator(RbcAttribute att) {
      super(att);
      mEstimator = att.getEstimator();
   }

   @Override
   public void estimateParameters(RDFDataSource source, RDFDataDescriptor desc, ClassEstimator classEst) throws RDFDatabaseException {
      mEstimator.estimateParameters(source, desc, classEst);
   }

   @Override
   public void estimateAllParameters(RDFDataSource source, RDFDataDescriptor desc, ClassEstimator classEst, TBox tBox) throws RDFDatabaseException {
      estimateParameters(source, desc, classEst);
   }
   
   @Override
   public double computeLikelihood(int classIndex, AttributeValue v) {
      return mEstimator.computeLikelihood(classIndex, v);
   }

   @Override
   public double computeLL() {
      return mEstimator.computeLL();
   }

   @Override
   public double computeDualLL() {
      return mEstimator.computeDualLL();
   }

}
