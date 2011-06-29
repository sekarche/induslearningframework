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
import airldm2.database.rdf.RDFDatabaseConnection;
import airldm2.database.rdf.RDFDatabaseConnectionFactory;
import airldm2.database.rdf.VirtuosoConnection;

public class RelationalBayesianClassifierTest {
   
   @Before
   public void setUp() {
   }
   
   @Test
   public void testSmallWithExactCounts() throws Exception {
      RDFDataDescriptor desc = RDFDataDescriptorParser.parse("rbc_example/smallDesc.txt");
      
      RDFDatabaseConnection conn = RDFDatabaseConnectionFactory.makeFromConfig();
      //named RDF graph that stores all training triples 
      SSDataSource trainSource = new RDFDataSource(conn, ":small");
      LDInstances trainInstances = new LDInstances();
      trainInstances.setDesc(desc);
      trainInstances.setDataSource(trainSource);
   
      RelationalBayesianClassifier rbc = new RelationalBayesianClassifier();
      rbc.buildClassifier(trainInstances);
      
      double[][][] counts = rbc.getCountsForTest();
      System.out.println(Arrays.deepToString(counts));
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
      
      Assert.assertEquals(5, rbc.getNumInstances());
   }
   
   @Test
   public void testMovies() throws Exception {
      testWithTrainInDBTestInDB("rbc_example/moviesDesc.txt", ":movies", ":movies");
   }
   
   @Test
   public void testMovies2() throws Exception {
      testWithTrainInDBTestInDB("rbc_example/moviesDescFilled_D.txt", "http://localhost:8890/sparql", ":default", "http://localhost:8892/sparql", ":default");
      testWithTrainInDBTestInDB("rbc_example/moviesDescFilled_I.txt", "http://localhost:8890/sparql", ":default", "http://localhost:8892/sparql", ":default");
   }
   
   @Test
   public void testCensus() throws Exception {
      testWithTrainInDBTestInDB("rbc_example/censusDescFilled_D.txt", "http://localhost:8890/sparql", ":census", "http://localhost:8890/sparql", ":census");
      testWithTrainInDBTestInDB("rbc_example/censusDescFilled_I.txt", "http://localhost:8890/sparql", ":census", "http://localhost:8890/sparql", ":census");
   }
   
   @Test
   public void testProject() throws Exception {
      testWithTrainInDBTestInDB("rbc_example/projectDesc.txt", ":projectTrain", ":projectTest");
   }
   
   @Test
   public void testHints() throws Exception {
      testWithTrainInDBTestInDB("rbc_example/nci_hintsDesc.txt", ":hints", ":hintsTest");
   }
   
   //Connects to a remote SPARQL - turn on only when needed
   //@Test
   public void testNCIHints() throws Exception {
      final String HINTS_DESC = "rbc_example/nci_hintsDesc.txt";
      final String LOGD_SPARQL = "http://logd.tw.rpi.edu/sparql";
      
      RDFDataDescriptor desc = RDFDataDescriptorParser.parse(HINTS_DESC);
      RDFDatabaseConnection conn = new VirtuosoConnection(LOGD_SPARQL);
      //named RDF graph that stores all training triples 
      SSDataSource trainSource = new RDFDataSource(conn);
      LDInstances trainInstances = new LDInstances();
      trainInstances.setDesc(desc);
      trainInstances.setDataSource(trainSource);
   
      RelationalBayesianClassifier rbc = new RelationalBayesianClassifier();
      rbc.buildClassifier(trainInstances);
   }
   
   private void testWithTrainInDBTestInDB(String descFile, String trainGraph, String testGraph) throws Exception {
      RDFDataDescriptor desc = RDFDataDescriptorParser.parse(descFile);
      //System.out.println(desc);
      
      RDFDatabaseConnection conn = RDFDatabaseConnectionFactory.makeFromConfig();
      //named RDF graph that stores all training triples 
      SSDataSource trainSource = new RDFDataSource(conn, trainGraph);
      LDInstances trainInstances = new LDInstances();
      trainInstances.setDesc(desc);
      trainInstances.setDataSource(trainSource);
   
      //named RDF graph that stores all test triples
      SSDataSource testSource = new RDFDataSource(conn, testGraph);
      LDInstances testInstances = new LDInstances();
      testInstances.setDesc(desc);
      testInstances.setDataSource(testSource);
   
      RelationalBayesianClassifier rbc = new RelationalBayesianClassifier();
      
      ConfusionMatrix matrix = Evaluation.evaluateRBCModel(rbc, trainInstances, testInstances);
      System.out.println(matrix.toString("===Confusion Matrix==="));
      System.out.println("Accuracy = " + (1.0 - matrix.errorRate()));
   }
   
   private void testWithTrainInDBTestInDB(String descFile, String trainSPARQL, String trainGraph, String testSPARQL, String testGraph) throws Exception {
      RDFDataDescriptor desc = RDFDataDescriptorParser.parse(descFile);
      //System.out.println(desc);
      
      RDFDatabaseConnection trainConn = new VirtuosoConnection(trainSPARQL);
      //named RDF graph that stores all training triples 
      SSDataSource trainSource = new RDFDataSource(trainConn, trainGraph);
      LDInstances trainInstances = new LDInstances();
      trainInstances.setDesc(desc);
      trainInstances.setDataSource(trainSource);
   
      RDFDatabaseConnection testConn = new VirtuosoConnection(testSPARQL);
      //named RDF graph that stores all test triples
      SSDataSource testSource = new RDFDataSource(testConn, testGraph);
      LDInstances testInstances = new LDInstances();
      testInstances.setDesc(desc);
      testInstances.setDataSource(testSource);
   
      RelationalBayesianClassifier rbc = new RelationalBayesianClassifier();
      
      ConfusionMatrix matrix = Evaluation.evaluateRBCModel(rbc, trainInstances, testInstances);
      System.out.println(matrix.toString("===Confusion Matrix==="));
      System.out.println("Accuracy = " + (1.0 - matrix.errorRate()));
   }

}
