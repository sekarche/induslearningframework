/**
 * 
 */
package airldm2.classifiers;

import airldm2.core.DefaultOptionHandlerImpl;
import airldm2.core.LDInstance;
import airldm2.core.LDInstances;
import airldm2.core.OptionHandler;

/**
 * @author neeraj
 * 
 */
public abstract class Classifier extends DefaultOptionHandlerImpl implements
      OptionHandler {

   /*
    * @param data set of instances serving as training data @exception
    * Exception if the classifier has not been generated successfully
    */
   public abstract void buildClassifier(LDInstances data) throws Exception;

   public abstract double classifyInstance(LDInstance instance)
         throws Exception;

   public abstract double[] distributionForInstance(LDInstance instance)
         throws Exception;

   public static void runClassifier(Classifier classifier, String[] options) {
      try {
         Evaluation eval = new Evaluation();
         System.out.println(eval.evaluateModel(classifier, options));
      } catch (Exception e) {
         System.out.println(e.getMessage());
         e.printStackTrace();
      }
   }

}
