package airldm2.classifiers.rl;

import java.net.URI;

import airldm2.classifiers.Classifier;
import airldm2.core.LDInstance;
import airldm2.core.LDInstances;

public class RelationalBayesianClassifier extends Classifier {

   @Override
   public void buildClassifier(LDInstances data) throws Exception {
      // TODO Auto-generated method stub

   }

   @Override
   public double classifyInstance(LDInstance instance) throws Exception {
      // TODO Auto-generated method stub
      return 0;
   }

   @Override
   public double[] distributionForInstance(LDInstance instance)
         throws Exception {
      // TODO Auto-generated method stub
      return null;
   }

   public double classifyInstance(LDInstances testInstances, URI test) {
      return 0.0;
   }

}
