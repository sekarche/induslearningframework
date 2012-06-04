package airldm2.classifiers.rl.estimator;

import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;

import airldm2.classifiers.rl.ontology.Cut;
import airldm2.classifiers.rl.ontology.TBox;
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
   public void mergeWith(List<AttributeEstimator> ests) {
   }
   
   @Override
   public Map<URI, AttributeEstimator> makeForAllHierarchy(TBox tBox) {
      return null;
   }
   
   public abstract void estimateAllParameters() throws RDFDatabaseException;

}
