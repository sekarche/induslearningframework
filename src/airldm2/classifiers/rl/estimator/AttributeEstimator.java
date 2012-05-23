package airldm2.classifiers.rl.estimator;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.exceptions.RDFDatabaseException;


public abstract class AttributeEstimator {
   
   protected RbcAttribute mAttribute;
   
   public AttributeEstimator(RbcAttribute att) {
      mAttribute = att;
   }
   
   public abstract void estimateParameters(RDFDataSource source, RDFDataDescriptor desc, ClassEstimator classEst) throws RDFDatabaseException;
   public abstract double computeLikelihood(int classIndex, AttributeValue v);
   
   public abstract double computeLL();
   public abstract double computeDualLL();
   
   @Override
   public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
   }
   
}
