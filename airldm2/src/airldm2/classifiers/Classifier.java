/**
 * 
 */
package airldm2.classifiers;

import airldm2.core.LDInstance;
import airldm2.core.LDInstances;
import airldm2.core.OptionHandler;

/**
 * @author neeraj
 * 
 */
public interface Classifier extends OptionHandler {

   /*
    * @param data set of instances serving as training data @exception
    * Exception if the classifier has not been generated successfully
    */
   public abstract void buildClassifier(LDInstances data) throws Exception;

   public abstract double classifyInstance(LDInstance instance)
         throws Exception;

   public double[] distributionForInstance(LDInstance instance)
         throws Exception;

}
