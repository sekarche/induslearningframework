package test;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import weka.classifiers.evaluation.ConfusionMatrix;
import airldm2.classifiers.Evaluation;
import airldm2.classifiers.rl.RelationalBayesianClassifier;
import airldm2.classifiers.rl.estimator.AttributeEstimator;
import airldm2.classifiers.rl.estimator.ClassEstimator;
import airldm2.classifiers.rl.estimator.ExponentialEstimator;
import airldm2.classifiers.rl.estimator.Histogram;
import airldm2.classifiers.rl.estimator.MultinomialEstimator;
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
      SSDataSource trainSource = new RDFDataSource(conn, desc, ":small");
      LDInstances trainInstances = new LDInstances();
      trainInstances.setDesc(desc);
      trainInstances.setDataSource(trainSource);
   
      RelationalBayesianClassifier rbc = new RelationalBayesianClassifier();
      rbc.buildClassifier(trainInstances);
      
      List<AttributeEstimator> counts = rbc.getCountsForTest();
      Assert.assertEquals(5, counts.size());
            
      Histogram[][] expectedValueHistograms = new Histogram[][] {
            Histogram.makeArray(new double[][]{ {1, 1, 3}, {0, 1, 0}, {0, 3, 0} }),
            Histogram.makeArray(new double[][]{ {0, 2, 0}, {2, 0, 0}, {0, 1, 0} }),
            Histogram.makeArray(new double[][]{ {0, 1, 1, 0}, {0, 0, 0, 1}, {0, 0, 0, 0} }),
            Histogram.makeArray(new double[][]{ {0, 2, 0, 0}, {1, 0, 0, 0}, {0, 0, 0, 0} }),
            Histogram.makeArray(new double[][]{ {0, 1, 0, 1}, {0, 0, 0, 1}, {0, 0, 0, 0} }),
         };
      Histogram[] expectedClassHistograms = Histogram.makeArray(new double[][]{
            {5, 1, 3},
            {2, 2, 1},
            {2, 1, 0},
            {2, 1, 0},
            {2, 1, 0},
         });
      
      for (int i = 0; i < counts.size(); i++) {
         MultinomialEstimator estimator = (MultinomialEstimator) counts.get(i);
         Histogram[] valueHistograms = estimator.getValueHistogramsForTest();
         Histogram classHistogram = estimator.getClassHistogramForTest();
         
         Assert.assertArrayEquals(expectedValueHistograms[i], valueHistograms);
         Assert.assertEquals(expectedClassHistograms[i], classHistogram);
      }
               
      ClassEstimator classEst = rbc.getClassCountsForTest();
      Assert.assertEquals(new Histogram(new double[] {2, 2, 1}), classEst.getClassHistogram());
      Assert.assertEquals(5, classEst.getNumInstances());
   }
   
   @Test
   public void testSmall2WithExactCounts() throws Exception {
      RDFDataDescriptor desc = RDFDataDescriptorParser.parse("rbc_example/smallDesc2.txt");
      
      RDFDatabaseConnection conn = RDFDatabaseConnectionFactory.makeFromConfig();
      //named RDF graph that stores all training triples 
      SSDataSource trainSource = new RDFDataSource(conn, desc, ":small");
      LDInstances trainInstances = new LDInstances();
      trainInstances.setDesc(desc);
      trainInstances.setDataSource(trainSource);
   
      RelationalBayesianClassifier rbc = new RelationalBayesianClassifier();
      rbc.buildClassifier(trainInstances);
      
      List<AttributeEstimator> counts = rbc.getCountsForTest();
      Assert.assertEquals(2, counts.size());
            
      Histogram[] expectedValueHistograms = new Histogram[] {
            new Histogram(new double[]{ 5, 1, 3 }),
            new Histogram(new double[]{ 100, 100, 0 }),
         };
      
      for (int i = 0; i < counts.size(); i++) {
         ExponentialEstimator estimator = (ExponentialEstimator) counts.get(i);
         Histogram valueHistograms = estimator.getValueSumsForTest();
         
         Assert.assertEquals(expectedValueHistograms[i], valueHistograms);
      }
   }
   
   @Test
   public void testMovies() throws Exception {
      testWithTrainInDBTestInDB("rbc_example/moviesDesc.txt", ":moviesTrain", ":moviesTest");
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
      SSDataSource trainSource = new RDFDataSource(conn, desc);
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
      SSDataSource trainSource = new RDFDataSource(conn, desc, trainGraph);
      LDInstances trainInstances = new LDInstances();
      trainInstances.setDesc(desc);
      trainInstances.setDataSource(trainSource);
   
      //named RDF graph that stores all test triples
      SSDataSource testSource = new RDFDataSource(conn, desc, testGraph);
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
      SSDataSource trainSource = new RDFDataSource(trainConn, desc, trainGraph);
      LDInstances trainInstances = new LDInstances();
      trainInstances.setDesc(desc);
      trainInstances.setDataSource(trainSource);
   
      RDFDatabaseConnection testConn = new VirtuosoConnection(testSPARQL);
      //named RDF graph that stores all test triples
      SSDataSource testSource = new RDFDataSource(testConn, desc, testGraph);
      LDInstances testInstances = new LDInstances();
      testInstances.setDesc(desc);
      testInstances.setDataSource(testSource);
   
      RelationalBayesianClassifier rbc = new RelationalBayesianClassifier();
      
      ConfusionMatrix matrix = Evaluation.evaluateRBCModel(rbc, trainInstances, testInstances);
      System.out.println(matrix.toString("===Confusion Matrix==="));
      System.out.println("Accuracy = " + (1.0 - matrix.errorRate()));
   }

}
