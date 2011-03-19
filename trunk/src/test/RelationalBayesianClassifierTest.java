package test;

import java.util.Arrays;

import org.junit.Assert;
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
   public void testSmall() throws Exception {
      RDFDataDescriptor desc = RDFDataDescriptorParser.parse("rbc_example/smallDesc.txt");
      //System.out.println(desc);
      
      //named RDF graph that stores all training triples 
      SSDataSource trainSource = new RDFDataSource(":small");
      LDInstances trainInstances = new LDInstances();
      trainInstances.setDesc(desc);
      trainInstances.setDataSource(trainSource);
   
      RelationalBayesianClassifier rbc = new RelationalBayesianClassifier();
      rbc.buildClassifier(trainInstances);
      double[][][] counts = rbc.getCountsForTest();
      Assert.assertTrue(Arrays.deepEquals(
         new double[][][] {
            { {1, 1, 3}, {0, 1, 0}, {0, 3, 0} },
            { {0, 2, 0}, {2, 0, 0}, {0, 1, 0} },
            { {0, 1, 1, 0}, {0, 0, 0, 1}, {0, 0, 0, 0} },
            { {0, 2, 0, 0}, {1, 0, 0, 0}, {0, 0, 0, 0} },
            { {0, 1, 0, 1}, {0, 0, 0, 1}, {0, 0, 0, 0} },
         }
         , counts));
      
      double[][] attributeClassCounts = rbc.getAttributeClassCountsForTest();
      Assert.assertTrue(Arrays.deepEquals(
            new double[][] {
               {5, 1, 3},
               {2, 2, 1},
               {2, 1, 0},
               {2, 1, 0},
               {2, 1, 0},
            }
            , attributeClassCounts));
         
      double[] classCounts = rbc.getClassCountsForTest();
      Assert.assertTrue(Arrays.equals(
            new double[] {2, 2, 1}
            , classCounts));
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
