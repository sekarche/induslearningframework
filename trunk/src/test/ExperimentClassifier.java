package test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryException;

import weka.classifiers.evaluation.ConfusionMatrix;
import weka.core.Matrix;
import airldm2.classifiers.Evaluation;
import airldm2.classifiers.rl.RBClassifier;
import airldm2.core.LDInstances;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataDescriptorParser;
import airldm2.core.rl.RDFDataSource;
import airldm2.database.rdf.RDFDatabaseConnection;
import airldm2.database.rdf.SPARQLQueryResult;
import airldm2.database.rdf.VirtuosoConnection;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.util.Timer;
import airldm2.util.Weigher;

public class ExperimentClassifier {
   
   private RDFDataSource mTrainData;
   private RDFDataSource mTestData;

   private ValueFactory ValueFac = new ValueFactoryImpl();
   
   private static boolean IsFinancial = true;
   private RDFDatabaseConnection TrainConn;
   private RDFDatabaseConnection TestConn;
   
   public static void main(String[] args) throws Exception {
      if (IsFinancial) 
         new ExperimentClassifier().run("rdfs_example/financialDescAll.txt", ":financial", "http://:financial/vocab/resource/loan");
      else
         new ExperimentClassifier().run("rdfs_example/coraDesc.txt", ":cora", "http://cora/vocab/paper");
   }
   
   private void run(String descFile, String context, String instanceURI) throws Exception {
      RDFDataDescriptor desc = RDFDataDescriptorParser.parse(descFile);
      
      setUpDataSource(desc, context, instanceURI);
      
      TrainConn = new VirtuosoConnection("jdbc:virtuoso://localhost:1111/charset=UTF-8/log_enable=2", "dba", "dba");
      TestConn = new VirtuosoConnection("jdbc:virtuoso://localhost:1117/charset=UTF-8/log_enable=2", "dba", "dba");

      List<URI> posInstances = getInstances(TrainConn, true);
      List<URI> negInstances = getInstances(TrainConn, false);
      Collections.shuffle(posInstances, new Random(0));
      Collections.shuffle(negInstances, new Random(0));
      
      BufferedWriter out = new BufferedWriter(new FileWriter("result.txt"));
      
      final int CROSS = 5;
      Matrix matrix = new Matrix(2, 2);
            
      for (int c = 0; c < CROSS; c++) {
         //remove train test
         removeFold(TrainConn, posInstances, CROSS, c);
         removeFold(TrainConn, negInstances, CROSS, c);
         removeInverseFold(TestConn, posInstances, CROSS, c);
         removeInverseFold(TestConn, negInstances, CROSS, c);
         
         //run
         LDInstances trainInstances = new LDInstances();
         trainInstances.setDesc(desc);
         trainInstances.setDataSource(mTrainData);
         LDInstances testInstances = new LDInstances();
         testInstances.setDesc(desc);
         testInstances.setDataSource(mTestData);
      
         RBClassifier model = new RBClassifier();
         ConfusionMatrix mat = Evaluation.evaluateRBCModel(model, trainInstances, testInstances);
//         RDTClassifier model = new RDTClassifier();
//         ConfusionMatrix mat = Evaluation.evaluateRDTModel(model, trainInstances, testInstances);
//         RRFClassifier model = new RRFClassifier(10, 3, 2);
//         ConfusionMatrix mat = Evaluation.evaluateRRFModel(model, trainInstances, testInstances);
//         OntologyRBClassifier model = new OntologyRBClassifier();
//         ConfusionMatrix mat = Evaluation.evaluateOntologyRBCModel(model, trainInstances, testInstances);
//         OntologyRDTClassifier model = new OntologyRDTClassifier();
//         ConfusionMatrix mat = Evaluation.evaluateOntologyRDTModel(model, trainInstances, testInstances);
//         OntologyRRFClassifier model = new OntologyRRFClassifier(10, 3, 2);
//         ConfusionMatrix mat = Evaluation.evaluateOntologyRRFModel(model, trainInstances, testInstances);
         
         matrix = matrix.add(mat);
         
         //add back train test
         addFold(TrainConn, posInstances, CROSS, c);
         addFold(TrainConn, negInstances, CROSS, c);
         addInverseFold(TestConn, posInstances, CROSS, c);
         addInverseFold(TestConn, negInstances, CROSS, c);
      }
      
      out.write(matrix.toString()); out.newLine();
      out.write("Total query size: " + Weigher.INSTANCE); out.newLine();
      out.write(Timer.INSTANCE.toString()); out.newLine();
      
      out.close();
   }
   
   private void setUpDataSource(RDFDataDescriptor desc, String graph, String instanceURI) throws RepositoryException {
      final String trainSPARQL = "http://localhost:8890/sparql";
      final String testSPARQL = "http://localhost:8896/sparql";
      
      RDFDatabaseConnection trainConn = new VirtuosoConnection(trainSPARQL);
      mTrainData = new RDFDataSource(trainConn, desc, graph);
      RDFDatabaseConnection testConn = new VirtuosoConnection(testSPARQL);
      mTestData = new RDFDataSource(testConn, desc, graph);
   }

   private void removeFold(RDFDatabaseConnection conn, List<URI> instances, int CROSS, int c) throws RDFDatabaseException {
      final int TEST_SIZE = instances.size() / CROSS;
   
      int testBegin = c * TEST_SIZE;
      int testEnd = testBegin + TEST_SIZE;
      if (c == CROSS - 1) {
         testEnd = instances.size();
      }
   
      int current = testBegin;
      for (int i = 0; i < instances.size(); i++) {
         if (current < testEnd) {
            int currentMod = current % instances.size();
            removeURI(conn, instances.get(currentMod));
         }
         current++;
      }
   }
   
   private void removeInverseFold(RDFDatabaseConnection conn, List<URI> instances, int CROSS, int c) throws RDFDatabaseException {
      final int TEST_SIZE = instances.size() / CROSS;
   
      int testBegin = c * TEST_SIZE;
      int testEnd = testBegin + TEST_SIZE;
      if (c == CROSS - 1) {
         testEnd = instances.size();
      }
   
      int current = testBegin;
      for (int i = 0; i < instances.size(); i++) {
         if (current >= testEnd) {
            int currentMod = current % instances.size();
            removeURI(conn, instances.get(currentMod));
         }
         current++;
      }
   }

   private void removeURI(RDFDatabaseConnection conn, URI uri) throws RDFDatabaseException {
      if (IsFinancial) {
         String deleteQuery = "DELETE FROM <:financial> { ?x a <http://:financial/vocab/resource/loan> . } WHERE { "
            + "FILTER(?x = <" + uri + ">) "
            + "?x a <http://:financial/vocab/resource/loan> . }";
         conn.executeUpdate(deleteQuery);
      } else {
         String deleteQuery = "DELETE FROM <:cora> { ?x a <http://cora/vocab/paper> . } WHERE { "
            + "FILTER(?x = <" + uri + ">) "
            + "?x a <http://cora/vocab/paper> . }";
         conn.executeUpdate(deleteQuery);
      }
   }
   
   private void addURI(RDFDatabaseConnection conn, URI uri) throws RDFDatabaseException {
      if (IsFinancial) {
         String addQuery = "INSERT INTO <:financial> { <" + uri + "> a <http://:financial/vocab/resource/loan> . } ";
         conn.executeUpdate(addQuery);
      } else {
         String addQuery = "INSERT INTO <:cora> { <" + uri + "> a <http://cora/vocab/paper> . } ";
         conn.executeUpdate(addQuery);
      }
   }
   
   private void addFold(RDFDatabaseConnection conn, List<URI> instances, int CROSS, int c) throws RDFDatabaseException {
      final int TEST_SIZE = instances.size() / CROSS;
   
      int testBegin = c * TEST_SIZE;
      int testEnd = testBegin + TEST_SIZE;
      if (c == CROSS - 1) {
         testEnd = instances.size();
      }
   
      int current = testBegin;
      for (int i = 0; i < instances.size(); i++) {
         if (current < testEnd) {
            int currentMod = current % instances.size();
            addURI(conn, instances.get(currentMod));
         }
         current++;
      }
   }
   
   private void addInverseFold(RDFDatabaseConnection conn, List<URI> instances, int CROSS, int c) throws RDFDatabaseException {
      final int TEST_SIZE = instances.size() / CROSS;
   
      int testBegin = c * TEST_SIZE;
      int testEnd = testBegin + TEST_SIZE;
      if (c == CROSS - 1) {
         testEnd = instances.size();
      }
   
      int current = testBegin;
      for (int i = 0; i < instances.size(); i++) {
         if (current >= testEnd) {
            int currentMod = current % instances.size();
            addURI(conn, instances.get(currentMod));
         }
         current++;
      }
   }
   
   private List<URI> getInstances(RDFDatabaseConnection trainConn, boolean isPositive) throws RDFDatabaseException {
      if (IsFinancial) {
         if (isPositive) {
            String query = "SELECT ?x FROM <:financial> WHERE { "
               + "?x a <http://:financial/vocab/resource/loan> . "
               + "?x <http://lisp.vse.cz/pkdd99/vocab/resource/loan_status> \"A\" . "
               + " } ORDER BY ?x";
            SPARQLQueryResult result = trainConn.executeQuery(query);
            return result.getURIList();
            
         } else {
            String query = "SELECT ?x FROM <:financial> WHERE { "
               + "?x a <http://:financial/vocab/resource/loan> . "
               + "?x <http://lisp.vse.cz/pkdd99/vocab/resource/loan_status> \"B\" . "
               + " } ORDER BY ?x";
            SPARQLQueryResult result = trainConn.executeQuery(query);
            return result.getURIList();

         }
      } else {
         if (isPositive) {
            String query = "SELECT ?x FROM <:cora> WHERE { "
               + "?x a <http://cora/vocab/paper> . "
               + "?x <http://cora/vocab/hasLabel> \"+\" . "
               + " } ORDER BY ?x";
            SPARQLQueryResult result = trainConn.executeQuery(query);
            return result.getURIList();
            
         } else {
            String query = "SELECT ?x FROM <:cora> WHERE { "
               + "?x a <http://cora/vocab/paper> . "
               + "?x <http://cora/vocab/hasLabel> \"-\" . "
               + " } ORDER BY ?x";
            SPARQLQueryResult result = trainConn.executeQuery(query);
            return result.getURIList();

         }
      }
   }

}
