package airldm2.classifiers.rl;

import java.net.URI;

import airldm2.classifiers.Classifier;
import airldm2.core.LDInstance;
import airldm2.core.LDInstances;
import airldm2.core.SSDataSource;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;

public class RelationalBayesianClassifier extends Classifier {

   private RDFDataSource mDataSource;
   private RDFDataDescriptor mDataDesc;
   private int mNumInstances;
   private int mClassIndex;

   // It goes: [attribute name][class value][attribute value]
   double[][][] mCounts;
   // It goes: [attribute name][class value]
   double[][] mClassCounts;
   
   @Override
   public void buildClassifier(LDInstances instances) throws Exception {
      mDataDesc = (RDFDataDescriptor) instances.getDesc();
      SSDataSource dataSource = instances.getDataSource();
   }
   
   @Override
   public double classifyInstance(LDInstance instance) throws Exception {
      return 0;
   }

   @Override
   public double[] distributionForInstance(LDInstance instance)
         throws Exception {
      return null;
   }

   public double classifyInstance(LDInstances testInstances, URI test) {
      return 0.0;
   }

}
