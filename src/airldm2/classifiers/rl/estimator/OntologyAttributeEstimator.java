package airldm2.classifiers.rl.estimator;

import airldm2.classifiers.rl.ontology.Cut;
import airldm2.classifiers.rl.ontology.TBox;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.exceptions.RDFDatabaseException;


public abstract class OntologyAttributeEstimator extends AttributeEstimator {

   protected TBox mTBox;
   
   protected Cut mCut;
   
   public OntologyAttributeEstimator(TBox tBox, RbcAttribute att) {
      super(att);
      mTBox = tBox;
   }
   
   public void setCut(Cut cut) {
      mCut = cut;
   }

   @Override
   public void mergeWith(AttributeEstimator est) {
   }
   
   public abstract void estimateAllParameters(RDFDataSource source, RDFDataDescriptor desc, ClassEstimator classEst) throws RDFDatabaseException;

}
