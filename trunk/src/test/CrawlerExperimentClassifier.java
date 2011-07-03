package test;

import java.io.BufferedWriter;
import java.io.FileWriter;

import org.openrdf.repository.RepositoryException;

import weka.classifiers.evaluation.ConfusionMatrix;
import airldm2.classifiers.Evaluation;
import airldm2.classifiers.rl.RelationalBayesianClassifier;
import airldm2.core.LDInstances;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataDescriptorParser;
import airldm2.core.rl.RDFDataSource;
import airldm2.database.rdf.RDFDatabaseConnection;
import airldm2.database.rdf.VirtuosoConnection;
import explore.ClassifierGuidedFeatureCrawler;

public class CrawlerExperimentClassifier {
   
   private RDFDataSource mTrainData;
   private RDFDataSource mTuneData;
   private RDFDataSource mTestData;

   public static void main(String[] args) throws Exception {
      new CrawlerExperimentClassifier().runMovie();
   }
   
   private void setUpDataSource() throws RepositoryException {
      final String trainSPARQL = "http://localhost:8890/sparql";
      final String tuneSPARQL = "http://localhost:8892/sparql";
      final String testSPARQL = "http://localhost:8894/sparql";
      final String graph = ":default";
      
      RDFDatabaseConnection trainConn = new VirtuosoConnection(trainSPARQL);
      mTrainData = new RDFDataSource(trainConn, graph);
      RDFDatabaseConnection tuneConn = new VirtuosoConnection(tuneSPARQL);
      mTuneData = new RDFDataSource(tuneConn, graph);
      RDFDatabaseConnection testConn = new VirtuosoConnection(testSPARQL);
      mTestData = new RDFDataSource(testConn, graph);
   }

   private void runMovie() throws Exception {
      setUpDataSource();
      
      final String emptyDescFile = "exp_movie/moviesDescEmpty.txt";
      
      BufferedWriter out = new BufferedWriter(new FileWriter("exp_movie/result.txt"));
      
      for (int n = 5; n <= 50; n += 5) {
         out.write("" + n); out.newLine();
         
         //Crawl
         ClassifierGuidedFeatureCrawler crawler = new ClassifierGuidedFeatureCrawler(mTrainData, mTuneData);
         
         String desc = "exp_movie/moviesDescFilled_RBC" + n + ".txt";
         RelationalBayesianClassifier rbc = crawler.crawl(emptyDescFile, desc, n);
         
         //Test
         ConfusionMatrix mat = test(desc, rbc);
         out.write(mat.toString()); out.newLine();
         out.write(""+(1 - mat.errorRate())); out.newLine();
      
         out.flush();
      }

      out.close();
   }
   
   private ConfusionMatrix test(String descFile, RelationalBayesianClassifier rbc) throws Exception {
      RDFDataDescriptor desc = RDFDataDescriptorParser.parse(descFile);
      //System.out.println(desc);
      
      LDInstances testInstances = new LDInstances();
      testInstances.setDesc(desc);
      testInstances.setDataSource(mTestData);
   
      ConfusionMatrix matrix = Evaluation.evaluateBuiltRBCModel(rbc, testInstances);
      return matrix;
   }
   
}
