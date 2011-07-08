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
import airldm2.classifiers.rl.RelationalBayesianClassifier;
import airldm2.core.LDInstances;
import airldm2.core.SSDataSource;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataDescriptorParser;
import airldm2.core.rl.RDFDataSource;
import airldm2.database.rdf.RDFDatabaseConnection;
import airldm2.database.rdf.RDFDatabaseConnectionFactory;
import airldm2.database.rdf.VirtuosoConnection;
import airldm2.util.CollectionUtil;
import explore.MIGuidedFeatureCrawler;
import explore.mitree.BFS;
import explore.mitree.BestScore;
import explore.mitree.OpenNodeVisitor;

public class CrawlerExperimentMI {
   
   private RepositoryConnection Census;
   private ValueFactory ValueFac = new ValueFactoryImpl();
   private URI CensusContext = ValueFac.createURI(":census");
   private URI STATE = ValueFac.createURI("http://www.rdfabout.com/rdf/usgov/geo/us/state");
   
   public static void main(String[] args) throws Exception {
      //new CrawlerExperimentMI().runCensus();
      new CrawlerExperimentMI().runMovie();
   }

   private void runMovie() throws Exception {
      RDFDatabaseConnection conn = RDFDatabaseConnectionFactory.makeFromConfig();
      RDFDataSource source = new RDFDataSource(conn, ":default");
      
      final String trainSPARQL = "http://localhost:8890/sparql";
      final String testSPARQL = "http://localhost:8896/sparql";
      final String graph = ":default";
      final String emptyDescFile = "exp_movie/moviesDescEmpty.txt";
      BufferedWriter out = new BufferedWriter(new FileWriter("exp_movie/result.txt"));
      
      final String[] METHOD = new String[] {"BFS", "BestScore"};
      final OpenNodeVisitor[] STRATEGY = new OpenNodeVisitor[] {new BFS(), new BestScore()};
      for (int m = 0; m < METHOD.length; m++) {
         MIGuidedFeatureCrawler crawler = new MIGuidedFeatureCrawler(source, emptyDescFile, STRATEGY[m]);
         out.write(METHOD[m]); out.newLine();
         
         for (int n = 5; n <= 100; n += 5) {
            //Crawl
            String desc = "exp_movie/moviesDescFilled_" + METHOD[m] + n + ".txt";
            crawler.crawl(desc, n, n);
            
            //Train
            RelationalBayesianClassifier rbc = train(desc, trainSPARQL, graph);
            
            //Test
            ConfusionMatrix mat = test(desc, rbc, testSPARQL, graph);
            out.write(mat.toString()); out.newLine();
            out.write(""+(1 - mat.errorRate())); out.newLine();
         
            out.write("" + n); out.newLine();
            out.flush();
         }
      }

      out.close();
   }

   private void runCensus() throws Exception {
      Repository census = new VirtuosoRepository("jdbc:virtuoso://localhost:1111/charset=UTF-8/log_enable=2", "dba", "dba", ":census");
      census.initialize();
      Census = census.getConnection();
      
      List<String> states = getStates();
      Collections.sort(states);
      Collections.shuffle(states, new Random(0));
      List<URI> stateURIs = makeURI(states);
      
      RDFDatabaseConnection conn = RDFDatabaseConnectionFactory.makeFromConfig();
      RDFDataSource source = new RDFDataSource(conn, ":census");
      
      final String SPARQL = "http://localhost:8890/sparql";
      final String GRAPH = ":census";
      final String emptyDescFile = "exp_census/censusDescEmpty.txt";
      final int CROSS = 52;
      Matrix matrix = null;
      BufferedWriter out = new BufferedWriter(new FileWriter("exp_census/result.txt"));
      
      final String[] METHOD = new String[] {"BFS", "BestScore"};
      final OpenNodeVisitor[] STRATEGY = new OpenNodeVisitor[] {new BFS(), new BestScore()};
      for (int m = 0; m < METHOD.length; m++) {
         MIGuidedFeatureCrawler crawler = new MIGuidedFeatureCrawler(source, emptyDescFile, STRATEGY[m]);
         crawler.setExclusion(new URI[] {new ValueFactoryImpl().createURI("http://logd.tw.rpi.edu/source/data-gov/dataset/311/vocab/")});
         out.write(METHOD[m]); out.newLine();
         
         for (int n = 5; n <= 100; n += 5) {
            matrix = new Matrix(2, 2);
            for (int c = 0; c < CROSS; c++) {
               int testBegin = c * stateURIs.size() / CROSS;
               int testEnd = (c+1) * stateURIs.size() / CROSS;
               //Remove Test states
               removeStates(stateURIs, testBegin, testEnd);
               
               //Crawl
               String desc = "exp_census/censusDescFilled_" + METHOD[m] + n + "_" + c + ".txt";
               crawler.crawl(desc, n, n);
               
               //Train
               RelationalBayesianClassifier rbc = train(desc, SPARQL, GRAPH);
               
               //Add Test states
               addStates(stateURIs, testBegin, testEnd);
               //Remove Train states
               removeInverseStates(stateURIs, testBegin, testEnd);
               
               //Test
               ConfusionMatrix mat = test(desc, rbc, SPARQL, GRAPH);
               out.write(mat.toString()); out.newLine();
               matrix = matrix.add(mat);
               //Add Train states
               addInverseStates(stateURIs, testBegin, testEnd);
            }
         
            out.write("" + n); out.newLine();
            out.write(matrix.toString()); out.newLine();
            out.flush();
         }
      }

      out.close();
      Census.close();
      census.shutDown();
   }
   
   private void removeStates(List<URI> stateURIs, int begin, int end) throws RepositoryException {
      for (int i = begin; i < end; i++) {
         Census.remove(stateURIs.get(i), RDF.TYPE, STATE, CensusContext);
      }
   }
   
   private void addStates(List<URI> stateURIs, int begin, int end) throws RepositoryException {
      for (int i = begin; i < end; i++) {
         Census.add(stateURIs.get(i), RDF.TYPE, STATE, CensusContext);
      }
   }
   
   private void removeInverseStates(List<URI> stateURIs, int begin, int end) throws RepositoryException {
      for (int i = 0; i < stateURIs.size(); i++) {
         if (i >= begin && i < end) continue;
         Census.remove(stateURIs.get(i), RDF.TYPE, STATE, CensusContext);
      }
   }
   
   private void addInverseStates(List<URI> stateURIs, int begin, int end) throws RepositoryException {
      for (int i = 0; i < stateURIs.size(); i++) {
         if (i >= begin && i < end) continue;
         Census.add(stateURIs.get(i), RDF.TYPE, STATE, CensusContext);
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

   private List<String> getStates() throws RepositoryException {
      List<String> states = CollectionUtil.makeList();
      RepositoryResult<Statement> rs = Census.getStatements(null, RDF.TYPE, STATE, false);
      while (rs.hasNext()) {
         Statement stat = rs.next();
         states.add(stat.getSubject().stringValue());
      }
      
      return states;
   }

   private RelationalBayesianClassifier train(String descFile, String sparql, String graph) throws Exception {
      RDFDataDescriptor desc = RDFDataDescriptorParser.parse(descFile);
      
      RDFDatabaseConnection trainConn = new VirtuosoConnection(sparql);
      //named RDF graph that stores all training triples 
      SSDataSource trainSource = new RDFDataSource(trainConn, graph);
      LDInstances trainInstances = new LDInstances();
      trainInstances.setDesc(desc);
      trainInstances.setDataSource(trainSource);
   
      RelationalBayesianClassifier rbc = new RelationalBayesianClassifier();
      rbc.buildClassifier(trainInstances);
      
      return rbc;
   }
   
   private ConfusionMatrix test(String descFile, RelationalBayesianClassifier rbc, String sparql, String graph) throws Exception {
      RDFDataDescriptor desc = RDFDataDescriptorParser.parse(descFile);
      //System.out.println(desc);
      
      RDFDatabaseConnection testConn = new VirtuosoConnection(sparql);
      //named RDF graph that stores all test triples
      SSDataSource testSource = new RDFDataSource(testConn, graph);
      LDInstances testInstances = new LDInstances();
      testInstances.setDesc(desc);
      testInstances.setDataSource(testSource);
   
      ConfusionMatrix matrix = Evaluation.evaluateBuiltRBCModel(rbc, testInstances);
      return matrix;
   }
   
}
