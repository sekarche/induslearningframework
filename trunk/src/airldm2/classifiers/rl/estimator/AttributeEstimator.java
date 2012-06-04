package airldm2.classifiers.rl.estimator;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openrdf.model.URI;

import airldm2.classifiers.rl.ontology.TBox;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.exceptions.RDFDatabaseException;


public abstract class AttributeEstimator {
   
   protected static Logger Log = Logger.getLogger("airldm2.classifiers.rl.estimator.AttributeEstimator");
   static { Log.setLevel(Level.WARNING); }
   
   protected RbcAttribute mAttribute;
   protected RDFDataSource mSource;
   protected RDFDataDescriptor mDesc;
   protected ClassEstimator mClassEst;
   
   public AttributeEstimator(RbcAttribute att) {
      mAttribute = att;
   }
   
   public void setDataSource(RDFDataSource source, RDFDataDescriptor desc, ClassEstimator classEst) {
      mSource = source;
      mDesc = desc;
      mClassEst = classEst;
   }
   
   protected int getClassSize() {
      return mClassEst.getClassHistogram().size();
   }
   
   protected double getClassCount(int c) {
      return mClassEst.getClassHistogram().get(c);
   }
   
   protected int getNumInstances() {
      return mClassEst.getNumInstances();
   }
   
   public abstract Map<URI,AttributeEstimator> makeForAllHierarchy(TBox tBox) throws RDFDatabaseException;
   
   public abstract void estimateParameters() throws RDFDatabaseException;
   public abstract double computeLikelihood(int classIndex, AttributeValue v);
   
   public abstract double computeLL();
   public abstract double computeDualLL();
   public abstract boolean isValid();
   public abstract double paramSize();

   public RbcAttribute getAttribute() {
      return mAttribute;
   }
   
   public double score() {
      return 0.0;
   }
   
   public abstract void mergeWith(List<AttributeEstimator> subEstimators);
   
   @Override
   public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
   }
   
}
