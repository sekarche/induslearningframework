package test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import virtuoso.sesame2.driver.VirtuosoRepository;
import weka.classifiers.evaluation.ConfusionMatrix;
import weka.core.Matrix;
import airldm2.classifiers.Evaluation;
import airldm2.classifiers.rl.RBClassifier;
import airldm2.core.LDInstances;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataDescriptorParser;
import airldm2.core.rl.RDFDataSource;
import airldm2.database.rdf.RDFDatabaseConnection;
import airldm2.database.rdf.VirtuosoConnection;
import airldm2.util.CollectionUtil;
import explore.ClassifierGuidedFeatureCrawler;

public class CrawlerExperimentClassifier {
   
   private RDFDataSource mTrainData;
   private RDFDataSource mSubtrainData;
   private RDFDataSource mTuneData;
   private RDFDataSource mTestData;

   private ValueFactory ValueFac = new ValueFactoryImpl();
   private URI CensusContext = ValueFac.createURI(":census");
   private URI STATE = ValueFac.createURI("http://www.rdfabout.com/rdf/usgov/geo/us/state");
   
   public static void main(String[] args) throws Exception {
      //new CrawlerExperimentClassifier().runMovie();
      new CrawlerExperimentClassifier().runCensus();
   }
   
   private void setUpDataSource(String graph) throws RepositoryException {
      final String trainSPARQL = "http://localhost:8890/sparql";
      final String subtrainSPARQL = "http://localhost:8892/sparql";
      final String tuneSPARQL = "http://localhost:8894/sparql";
      final String testSPARQL = "http://localhost:8896/sparql";
      
      RDFDatabaseConnection trainConn = new VirtuosoConnection(trainSPARQL);
      mTrainData = new RDFDataSource(trainConn, graph);
      RDFDatabaseConnection subtrainConn = new VirtuosoConnection(subtrainSPARQL);
      mSubtrainData = new RDFDataSource(subtrainConn, graph);
      RDFDatabaseConnection tuneConn = new VirtuosoConnection(tuneSPARQL);
      mTuneData = new RDFDataSource(tuneConn, graph);
      RDFDatabaseConnection testConn = new VirtuosoConnection(testSPARQL);
      mTestData = new RDFDataSource(testConn, graph);
   }

   private void runMovie() throws Exception {
      setUpDataSource(":default");
      
      final String emptyDescFile = "exp_movie/moviesDescEmpty.txt";
      
      BufferedWriter out = new BufferedWriter(new FileWriter("exp_movie/result.txt"));
      ClassifierGuidedFeatureCrawler crawler = new ClassifierGuidedFeatureCrawler(mTrainData, mSubtrainData, mTuneData, emptyDescFile);
      
      for (int n = 1; n <= 40; n++) {
         out.write("" + n); out.newLine();
         
         String desc = "exp_movie/moviesDescFilled_RBC" + n + ".txt";
         RBClassifier rbc = crawler.crawlAndWriteDesc(desc, n);
         
         //Test
         ConfusionMatrix mat = test(desc, rbc);
         out.write(mat.toString()); out.newLine();
         out.write(""+(1 - mat.errorRate())); out.newLine();
      
         out.flush();
      }

      out.close();
   }
   
   private ConfusionMatrix test(String descFile, RBClassifier rbc) throws Exception {
      RDFDataDescriptor desc = RDFDataDescriptorParser.parse(descFile);
      //System.out.println(desc);
      
      LDInstances testInstances = new LDInstances();
      testInstances.setDesc(desc);
      testInstances.setDataSource(mTestData);
   
      ConfusionMatrix matrix = Evaluation.evaluateBuiltRBCModel(rbc, testInstances);
      return matrix;
   }
   
   private void runCensus() throws Exception {
      setUpDataSource(":census");
      Repository trainRepo = new VirtuosoRepository("jdbc:virtuoso://localhost:1111/charset=UTF-8/log_enable=2", "dba", "dba", ":census");
      trainRepo.initialize();
      RepositoryConnection trainConn = trainRepo.getConnection();
      Repository subtrainRepo = new VirtuosoRepository("jdbc:virtuoso://localhost:1113/charset=UTF-8/log_enable=2", "dba", "dba", ":census");
      subtrainRepo.initialize();
      RepositoryConnection subtrainConn = subtrainRepo.getConnection();
      Repository tuneRepo = new VirtuosoRepository("jdbc:virtuoso://localhost:1115/charset=UTF-8/log_enable=2", "dba", "dba", ":census");
      tuneRepo.initialize();
      RepositoryConnection tuneConn = tuneRepo.getConnection();
      Repository testRepo = new VirtuosoRepository("jdbc:virtuoso://localhost:1117/charset=UTF-8/log_enable=2", "dba", "dba", ":census");
      testRepo.initialize();
      RepositoryConnection testConn = testRepo.getConnection();
      
      List<String> states = getStates(trainConn);
      Collections.sort(states);
      Collections.shuffle(states, new Random(0));
      List<URI> stateURIs = makeURI(states);
      
      final String emptyDescFile = "exp_census/censusDescEmpty.txt";
      BufferedWriter out = new BufferedWriter(new FileWriter("exp_census/result.txt"));
      
      final int CROSS = 13;
      final int STEPS = 20;
      final int STEP_SIZE = 5;
      Matrix[] matrix = new Matrix[STEPS];
      for (int i = 0; i < STEPS; i++) {
         matrix[i] = new Matrix(2, 2);
      }
      
      final int TEST_SIZE = stateURIs.size() / CROSS;
      final int TUNE_SIZE = 24;
      
      for (int c = 0; c < CROSS; c++) {
         int testBegin = c * TEST_SIZE;
         int testEndTuneBegin = testBegin + TEST_SIZE;
         int tuneEnd = testEndTuneBegin + TUNE_SIZE;
         
         //remove train tune test
         removeStates(trainConn, stateURIs, testBegin, testEndTuneBegin);
         removeStates(subtrainConn, stateURIs, testBegin, tuneEnd);
         removeInverseStates(tuneConn, stateURIs, testEndTuneBegin, tuneEnd);
         removeInverseStates(testConn, stateURIs, testBegin, testEndTuneBegin);
         
         ClassifierGuidedFeatureCrawler crawler = new ClassifierGuidedFeatureCrawler(mTrainData, mSubtrainData, mTuneData, emptyDescFile);
         crawler.setExclusion(new URI[] {new ValueFactoryImpl().createURI("http://logd.tw.rpi.edu/source/data-gov/dataset/311/vocab/")});
         
         for (int i = 0; i < STEPS; i++) {
            int n = (i+1) * STEP_SIZE;
            String desc = "exp_census/censusDescFilled_RBC" + n + "_" + c + ".txt";
            RBClassifier rbc = crawler.crawlAndWriteDesc(desc, n);
            
            //Test
            ConfusionMatrix mat = test(desc, rbc);
            matrix[i] = matrix[i].add(mat);
         }
         
         //add back train tune test
         addStates(trainConn, stateURIs, testBegin, testEndTuneBegin);
         addStates(subtrainConn, stateURIs, testBegin, tuneEnd);
         addInverseStates(tuneConn, stateURIs, testEndTuneBegin, tuneEnd);
         addInverseStates(testConn, stateURIs, testBegin, testEndTuneBegin);
      }
      
      for (int i = 0; i < STEPS; i++) {
         out.write("" + ((i+1) * STEP_SIZE)); out.newLine();
         out.write(matrix[i].toString()); out.newLine();
      }
      
      out.close();
      trainConn.close();
      trainRepo.shutDown();
      subtrainConn.close();
      subtrainRepo.shutDown();
      tuneConn.close();
      tuneRepo.shutDown();
      testConn.close();
      testRepo.shutDown();
   }
   
   private void removeStates(RepositoryConnection conn, List<URI> stateURIs, int begin, int end) throws RepositoryException {
      int current = begin;
      for (int i = 0; i < stateURIs.size(); i++) {
         if (current < end) {
            int currentMod = current % stateURIs.size();
            conn.remove(stateURIs.get(currentMod), RDF.TYPE, STATE, CensusContext);
         }
         current++;
      }
   }
   
   private void addStates(RepositoryConnection conn, List<URI> stateURIs, int begin, int end) throws RepositoryException {
      int current = begin;
      for (int i = 0; i < stateURIs.size(); i++) {
         if (current < end) {
            int currentMod = current % stateURIs.size();
            conn.add(stateURIs.get(currentMod), RDF.TYPE, STATE, CensusContext);
         }
         current++;
      }
   }
   
   private void removeInverseStates(RepositoryConnection conn, List<URI> stateURIs, int begin, int end) throws RepositoryException {
      int current = begin;
      for (int i = 0; i < stateURIs.size(); i++) {
         if (current >= end) {
            int currentMod = current % stateURIs.size();
            conn.remove(stateURIs.get(currentMod), RDF.TYPE, STATE, CensusContext);
         }
         current++;
      }
   }
   
   private void addInverseStates(RepositoryConnection conn, List<URI> stateURIs, int begin, int end) throws RepositoryException {
      int current = begin;
      for (int i = 0; i < stateURIs.size(); i++) {
         if (current >= end) {
            int currentMod = current % stateURIs.size();
            conn.add(stateURIs.get(currentMod), RDF.TYPE, STATE, CensusContext);
         }
         current++;
      }
   }

   private List<URI> makeURI(List<String> states) {
      List<URI> uris = CollectionUtil.makeList();
      for (int i = 0; i < states.size(); i++) {
         String state = states.get(i);
         uris.add(ValueFac.createURI(state));
      }
      return uris;
   }

   private List<String> getStates(RepositoryConnection conn) throws RepositoryException {
      List<String> states = CollectionUtil.makeList();
      RepositoryResult<Statement> rs = conn.getStatements(null, RDF.TYPE, STATE, false);
      while (rs.hasNext()) {
         Statement stat = rs.next();
         states.add(stat.getSubject().stringValue());
      }
      
      return states;
   }

}
