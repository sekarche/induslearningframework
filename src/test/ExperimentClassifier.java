package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
import airldm2.classifiers.rl.OntologyRBClassifier;
import airldm2.classifiers.rl.OntologyRRFClassifier;
import airldm2.classifiers.rl.ontology.Cut;
import airldm2.classifiers.rl.ontology.TBox;
import airldm2.core.LDInstances;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataDescriptorParser;
import airldm2.core.rl.RDFDataSource;
import airldm2.database.rdf.RDFDatabaseConnection;
import airldm2.database.rdf.RDFDatabaseConnectionFactory;
import airldm2.database.rdf.SPARQLQueryResult;
import airldm2.database.rdf.VirtuosoConnection;
import airldm2.exceptions.RDFDataDescriptorFormatException;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.util.Timer;
import airldm2.util.Weigher;

import com.clarkparsia.pellint.util.CollectionUtil;

public class ExperimentClassifier {
   
   private RDFDataSource mTrainData;
   private RDFDataSource mTestData;

   private ValueFactory ValueFac = new ValueFactoryImpl();
   
   private RDFDatabaseConnection TrainConn;
   private RDFDatabaseConnection TestConn;
   
   public static void main(String[] args) throws Exception {
      new ExperimentClassifier().run("rdfs_example/flickrDescH.txt", ":flickr", "http://flickr/vocab/user");
      //new ExperimentClassifier().runCut("rdfs_example/flickrDescH.txt", ":flickr", "http://flickr/vocab/user");
      
      //new ExperimentClassifier().run("rdfs_example/lastfmDesc.txt", ":lastfm", "http://lastfm/vocab/user");
      //new ExperimentClassifier().runCut("rdfs_example/lastfmDescH.txt", ":lastfm", "http://lastfm/vocab/user");
      
//      new ExperimentClassifier().runComm("rdfs_example/flickrDescH.txt", ":subset", "http://flickr/vocab/user");
//      SubclassReasoner2.main(null);
//      new ExperimentClassifier().runCommMat("rdfs_example/flickrDescH.txt", ":subset", "http://flickr/vocab/user");
   }
   
   private void run(String descFile, String context, String instanceURI) throws Exception {
      TrainConn = new VirtuosoConnection("jdbc:virtuoso://localhost:1113/charset=UTF-8/log_enable=2", "dba", "dba");
      TestConn = new VirtuosoConnection("jdbc:virtuoso://localhost:1115/charset=UTF-8/log_enable=2", "dba", "dba");

      List<URI> posInstances = getInstances(TrainConn, true);
      List<URI> negInstances = getInstances(TrainConn, false);
      Collections.shuffle(posInstances, new Random(0));
      Collections.shuffle(negInstances, new Random(0));
      
      BufferedWriter out = new BufferedWriter(new FileWriter("result.txt"));
      
      final int CROSS = 5;
      Matrix matrix = new Matrix(2, 2);
            
      for (int c = 0; c < CROSS; c++) {
         RDFDataDescriptor desc = RDFDataDescriptorParser.parse(descFile);
         setUpDataSource(desc, context, instanceURI);
         
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
      
//         RBClassifier model = new RBClassifier();
//         ConfusionMatrix mat = Evaluation.evaluateRBCModel(model, trainInstances, testInstances);
//         RRFClassifier model = new RRFClassifier(51, 30, 3);
//         ConfusionMatrix mat = Evaluation.evaluateRRFModel(model, trainInstances, testInstances);
         OntologyRBClassifier model = new OntologyRBClassifier(false);
         ConfusionMatrix mat = Evaluation.evaluateOntologyRBCModel(model, trainInstances, testInstances);
//         OntologyRRFClassifier model = new OntologyRRFClassifier(11, 30, 3);
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
   
   private void runCut(String descFile, String context, String instanceURI) throws Exception {
      RDFDataDescriptor desc = RDFDataDescriptorParser.parse(descFile);
      setUpDataSource(desc, context, instanceURI);
      TrainConn = new VirtuosoConnection("jdbc:virtuoso://localhost:1113/charset=UTF-8/log_enable=2", "dba", "dba");
      TestConn = new VirtuosoConnection("jdbc:virtuoso://localhost:1115/charset=UTF-8/log_enable=2", "dba", "dba");

      List<URI> posInstances = getInstances(TrainConn, true);
      List<URI> negInstances = getInstances(TrainConn, false);
      Collections.shuffle(posInstances, new Random(0));
      Collections.shuffle(negInstances, new Random(0));
      
      BufferedWriter out = new BufferedWriter(new FileWriter("result.txt"));
      
      List<Cut> cuts = readCuts();
      for (int i = 0; i < cuts.size(); i++) {
         Cut cut = cuts.get(i);
         
         final int CROSS = 5;
         Matrix matrix = new Matrix(2, 2);
         for (int c = 0; c < CROSS; c++) {
            desc = RDFDataDescriptorParser.parse(descFile);
            setUpDataSource(desc, context, instanceURI);
            
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
         
            OntologyRBClassifier model = new OntologyRBClassifier(cut);
            ConfusionMatrix mat = Evaluation.evaluateOntologyRBCModel(model, trainInstances, testInstances);
            
            matrix = matrix.add(mat);
            
            //add back train test
            addFold(TrainConn, posInstances, CROSS, c);
            addFold(TrainConn, negInstances, CROSS, c);
            addInverseFold(TestConn, posInstances, CROSS, c);
            addInverseFold(TestConn, negInstances, CROSS, c);
         }
         
         out.write("Cut size = " + cut.size()); out.newLine();
         out.write(matrix.toString()); out.newLine();
         //out.write("Total query size: " + Weigher.INSTANCE); out.newLine();
         //out.write(Timer.INSTANCE.toString()); out.newLine();
         out.flush();
      }
      
      out.close();
   }
   
   private void runComm(String descFile, String context, String instanceURI) throws Exception {
      TrainConn = new VirtuosoConnection("jdbc:virtuoso://localhost:1113/charset=UTF-8/log_enable=2", "dba", "dba");

      BufferedWriter out = new BufferedWriter(new FileWriter("resultRaw.txt"));
      
      LDInstances trainInstances = resetTrainingInstances(descFile, context, instanceURI);
      List<Cut> allCuts = readCuts();
      int[] cutSizes = new int[] {2000};
      for (int i = 0; i < cutSizes.length; i++) {
         Cut cut = chooseCut(allCuts, cutSizes[i]);
         
         trainInstances = resetTrainingInstances(descFile, context, instanceURI);
         OntologyRBClassifier rbcOpt = new OntologyRBClassifier(cut);
         rbcOpt.setOptimizeOntology(true);
         RDFDatabaseConnectionFactory.QUERY_INFERENCE = false;
         rbcOpt.buildClassifier(trainInstances);
         out.write("RBC (cut " + cut.size() + ") optimized:"); out.newLine();
         out.write("Total query size: " + Weigher.INSTANCE); out.newLine();
         out.write(Timer.INSTANCE.toString()); out.newLine();

//         trainInstances = resetTrainingInstances(descFile, context, instanceURI);
//         OntologyRBClassifier rbcInf = new OntologyRBClassifier(cut);
//         rbcInf.setOptimizeOntology(false);
//         RDFDatabaseConnectionFactory.QUERY_INFERENCE = true;
//         rbcInf.buildClassifier(trainInstances);
//         out.write("RBC (cut " + cut.size() + ") online inf:"); out.newLine();
//         out.write("Total query size: " + Weigher.INSTANCE); out.newLine();
//         out.write(Timer.INSTANCE.toString()); out.newLine();
      }
      
//      trainInstances = resetTrainingInstances(descFile, context, instanceURI);
//      OntologyRRFClassifier rrf = new OntologyRRFClassifier(11, 30, 3);
//      RDFDatabaseConnectionFactory.QUERY_INFERENCE = true;
//      rrf.buildClassifier(trainInstances);
//      out.write("RRF online inf:"); out.newLine();
//      out.write("Total query size: " + Weigher.INSTANCE); out.newLine();
//      out.write(Timer.INSTANCE.toString()); out.newLine();
      
      out.close();
   }
   
   private void runCommMat(String descFile, String context, String instanceURI) throws Exception {
      TrainConn = new VirtuosoConnection("jdbc:virtuoso://localhost:1113/charset=UTF-8/log_enable=2", "dba", "dba");

      BufferedWriter out = new BufferedWriter(new FileWriter("resultMat.txt"));
      
      LDInstances trainInstances = resetTrainingInstances(descFile, context, instanceURI);
      List<Cut> allCuts = readCuts();
      int[] cutSizes = new int[] {200, 2000};
      for (int i = 0; i < cutSizes.length; i++) {
         Cut cut = chooseCut(allCuts, cutSizes[i]);
         
         trainInstances = resetTrainingInstances(descFile, context, instanceURI);
         OntologyRBClassifier rbcMat = new OntologyRBClassifier(cut);
         rbcMat.setOptimizeOntology(false);
         RDFDatabaseConnectionFactory.QUERY_INFERENCE = false;
         rbcMat.buildClassifier(trainInstances);
         out.write("RBC (cut " + cut.size() + ") materialized:"); out.newLine();
         out.write("Total query size: " + Weigher.INSTANCE); out.newLine();
         out.write(Timer.INSTANCE.toString()); out.newLine();
      }
      
      trainInstances = resetTrainingInstances(descFile, context, instanceURI);
      OntologyRRFClassifier rrf = new OntologyRRFClassifier(11, 30, 3);
      RDFDatabaseConnectionFactory.QUERY_INFERENCE = false;
      rrf.buildClassifier(trainInstances);
      out.write("RRF materialized:"); out.newLine();
      out.write("Total query size: " + Weigher.INSTANCE); out.newLine();
      out.write(Timer.INSTANCE.toString()); out.newLine();
      
      out.close();
   }
   
   private LDInstances resetTrainingInstances(String descFile, String context, String instanceURI) throws IOException, RDFDataDescriptorFormatException, RepositoryException {
      RDFDataDescriptor desc = RDFDataDescriptorParser.parse(descFile);
      setUpDataSource(desc, context, instanceURI);
      LDInstances trainInstances = new LDInstances();
      trainInstances.setDesc(desc);
      trainInstances.setDataSource(mTrainData);
   
      Weigher.INSTANCE.reset();
      Timer.INSTANCE.reset();
      return trainInstances;
   }

   private Cut chooseCut(List<Cut> cuts, int i) {
      for (Cut cut : cuts) {
         if (cut.size() >= i) {
            return cut;
         }
      }
      return null;
   }

   private List<Cut> readCuts() throws IOException, RDFDatabaseException {
      List<Cut> allCuts = CollectionUtil.makeList();
      
      TBox tBox = mTrainData.getTBox();
      
      BufferedReader in = new BufferedReader(new FileReader("cutFiltered.txt"));
      
      String line;
      while ((line=in.readLine()) != null) {
         String[] members = line.substring(1, line.length() - 1).split(",");
         List<URI> uris = CollectionUtil.makeList();
         for (int i = 0; i < members.length; i++) {
            URI uri = ValueFac.createURI(members[i].trim());
            uris.add(uri);
         }
         Cut cut = new Cut(tBox, uris);
         allCuts.add(cut);
      }
      in.close();
      
      return allCuts;
   }

   private void setUpDataSource(RDFDataDescriptor desc, String graph, String instanceURI) throws RepositoryException {
      final String trainSPARQL = "http://localhost:8893/sparql";
      final String testSPARQL = "http://localhost:8895/sparql";
      
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

//   private void removeURI(RDFDatabaseConnection conn, URI uri) throws RDFDatabaseException {
//      String deleteQuery = "DELETE FROM <:lastfm> { ?x a <http://lastfm/vocab/user> . } WHERE { "
//         + "FILTER(?x = <" + uri + ">) "
//         + "?x a <http://lastfm/vocab/user> . }";
//      conn.executeUpdate(deleteQuery);
//   }
//   
//   private void addURI(RDFDatabaseConnection conn, URI uri) throws RDFDatabaseException {
//      String addQuery = "INSERT INTO <:lastfm> { <" + uri + "> a <http://lastfm/vocab/user> . } ";
//      conn.executeUpdate(addQuery);
//   }
   
   private void removeURI(RDFDatabaseConnection conn, URI uri) throws RDFDatabaseException {
      String deleteQuery = "DELETE FROM <:flickr> { ?x a <http://flickr/vocab/user> . } WHERE { "
         + "FILTER(?x = <" + uri + ">) "
         + "?x a <http://flickr/vocab/user> . }";
      conn.executeUpdate(deleteQuery);
   }
   
   private void addURI(RDFDatabaseConnection conn, URI uri) throws RDFDatabaseException {
      String addQuery = "INSERT INTO <:flickr> { <" + uri + "> a <http://flickr/vocab/user> . } ";
      conn.executeUpdate(addQuery);
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
   
//   private List<URI> getInstances(RDFDatabaseConnection trainConn, boolean isPositive) throws RDFDatabaseException {
//      if (isPositive) {
//         String query = "SELECT ?x FROM <:lastfm> WHERE { "
//            + "?x a <http://lastfm/vocab/user> . "
//            + "?x <http://lastfm/vocab/hasGroup> \"group0\" . "
//            + " } ORDER BY ?x";
//         SPARQLQueryResult result = trainConn.executeQuery(query);
//         return result.getURIList();
//         
//      } else {
//         String query = "SELECT ?x FROM <:lastfm> WHERE { "
//            + "?x a <http://lastfm/vocab/user> . "
//            + "?x <http://lastfm/vocab/hasGroup> \"group1\" . "
//            + " } ORDER BY ?x";
//         SPARQLQueryResult result = trainConn.executeQuery(query);
//         return result.getURIList();
//
//      }
//   }

   private List<URI> getInstances(RDFDatabaseConnection trainConn, boolean isPositive) throws RDFDatabaseException {
      if (isPositive) {
         String query = "SELECT ?x FROM <:flickr> WHERE { "
            + "?x a <http://flickr/vocab/user> . "
            + "?x <http://flickr/vocab/hasGroup> \"AbandonedCalifornia\" . "
            + " } ORDER BY ?x";
         SPARQLQueryResult result = trainConn.executeQuery(query);
         return result.getURIList();
         
      } else {
         String query = "SELECT ?x FROM <:flickr> WHERE { "
            + "?x a <http://flickr/vocab/user> . "
            + "?x <http://flickr/vocab/hasGroup> \"FindingHome\" . "
            + " } ORDER BY ?x";
         SPARQLQueryResult result = trainConn.executeQuery(query);
         return result.getURIList();

      }
   }
   
}
