package airldm2.classifiers.rl.estimator;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.exceptions.RDFDatabaseException;


public abstract class AttributeEstimator {
   
   protected static Logger Log = Logger.getLogger("airldm2.classifiers.rl.estimator.AttributeEstimator");
   static { Log.setLevel(Level.WARNING); }
   
   protected RbcAttribute mAttribute;
   
   public AttributeEstimator(RbcAttribute att) {
      mAttribute = att;
   }
   
   public abstract void estimateParameters(RDFDataSource source, RDFDataDescriptor desc, ClassEstimator classEst) throws RDFDatabaseException;
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
