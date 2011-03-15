package test;

import org.junit.Before;
import org.junit.Test;

import weka.classifiers.evaluation.ConfusionMatrix;
import airldm2.classifiers.Evaluation;
import airldm2.classifiers.rl.RelationalBayesianClassifier;
import airldm2.core.LDInstances;
import airldm2.core.SSDataSource;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataDescriptorParser;
import airldm2.core.rl.RDFDataSource;

public class RelationalBayesianClassifierTest {
   
   @Before
   public void setUp() {
   }
   
   @Test
   public void testMovies() throws Exception {
      testWithTrainInDBTestInDB("rbc_example/moviesDesc.txt", ":moviesTrain", ":moviesTest");
   }
   
   @Test
   public void testProject() throws Exception {
      testWithTrainInDBTestInDB("rbc_example/projectDesc.txt", ":projectTrain", ":projectTest");
   }

   private void testWithTrainInDBTestInDB(String descFile, String trainGraph, String testGraph) throws Exception {
      RDFDataDescriptor desc = RDFDataDescriptorParser.parse(descFile);
      //System.out.println(desc);
      
      //named RDF graph that stores all training triples 
      SSDataSource trainSource = new RDFDataSource(trainGraph);
      LDInstances trainInstances = new LDInstances();
      trainInstances.setDesc(desc);
      trainInstances.setDataSource(trainSource);
   
      //named RDF graph that stores all test triples
      SSDataSource testSource = new RDFDataSource(testGraph);
      LDInstances testInstances = new LDInstances();
      testInstances.setDesc(desc);
      testInstances.setDataSource(testSource);
   
      RelationalBayesianClassifier rbc = new RelationalBayesianClassifier();
      
      ConfusionMatrix matrix = Evaluation.evaluateRBCModel(rbc, trainInstances, testInstances);
      System.out.println(matrix.toString("===Confusion Matrix==="));
   }

}
