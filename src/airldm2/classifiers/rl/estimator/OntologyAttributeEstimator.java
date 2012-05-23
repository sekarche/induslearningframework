package airldm2.classifiers.rl.estimator;

import airldm2.classifiers.rl.ontology.Cut;
import airldm2.core.rl.RbcAttribute;


public abstract class OntologyAttributeEstimator extends AttributeEstimator {

   protected Cut mCut;
   
   public OntologyAttributeEstimator(RbcAttribute att) {
      super(att);
   }
   
   public void setCut(Cut cut) {
      mCut = cut;
   }

}
