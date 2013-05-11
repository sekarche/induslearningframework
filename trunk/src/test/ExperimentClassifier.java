package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryException;

import weka.classifiers.evaluation.ConfusionMatrix;
import airldm2.classifiers.Evaluation;
import airldm2.classifiers.rl.OntologyRBClassifier;
import airldm2.classifiers.rl.RBClassifier;
import airldm2.classifiers.rl.ontology.Cut;
import airldm2.classifiers.rl.ontology.TBox;
import airldm2.core.LDInstances;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataDescriptorParser;
import airldm2.core.rl.RDFDataSource;
import airldm2.database.rdf.RDFDatabaseConnection;
import airldm2.database.rdf.RDFDatabaseConnectionFactory;
import airldm2.database.rdf.VirtuosoConnection;
import airldm2.exceptions.RDFDataDescriptorFormatException;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.util.Timer;
import airldm2.util.Weigher;
import airldm2.util.rdf.SubclassReasoner2;

import com.clarkparsia.pellint.util.CollectionUtil;

public class ExperimentClassifier {
   
   private RDFDataSource mTrainData;
   private RDFDataSource mTestData;

   private ValueFactory ValueFac = new ValueFactoryImpl();
   
   private RDFDatabaseConnection TrainConn;
   private RDFDatabaseConnection TestConn;
   
   public static void main(String[] args) throws Exception {
      //int FOLD = Integer.parseInt(args[0]);
      
//      new ExperimentClassifier().runRBC("rdfs_example/lastfmDescSet.txt", ":data", "resultLastfmRBCBer" + FOLD + ".txt");
//      new ExperimentClassifier().runRBC("rdfs_example/lastfmDescHist.txt", ":data", "resultLastfmRBCMul" + FOLD + ".txt");
      
//      new ExperimentClassifier().runHRBC("rdfs_example/lastfmDescSetH.txt", ":data", "resultLastfmHRBCBer" + FOLD + ".txt");
//      new ExperimentClassifier().runHRBC("rdfs_example/lastfmDescHistH.txt", ":data", "resultLastfmHRBCMul" + FOLD + ".txt");
      
//      new ExperimentClassifier().runCut("rdfs_example/flickrDescHistH.txt", ":data", "resultMul.txt", 1113, 1115);
//      new ExperimentClassifier().runCut("rdfs_example/flickrDescSetH.txt", ":data", "resultBer.txt", 1113, 1115);
      
//      new ExperimentClassifier().runCut("rdfs_example/lastfmDescHistH.txt", ":data", "result0Mul.txt", 1111, 1113);
//      new ExperimentClassifier().runCut("rdfs_example/lastfmDescSetH.txt", ":data", "result0Ber.txt", 1111, 1113);
//      new ExperimentClassifier().runCut("rdfs_example/lastfmDescHistH.txt", ":data", "result1Mul.txt", 1113, 1111);
//      new ExperimentClassifier().runCut("rdfs_example/lastfmDescSetH.txt", ":data", "result1Ber.txt", 1113, 1111);
      
      int SIZE = 1000;
      new ExperimentClassifier().runComm("rdfs_example/lastfmDescHistH.txt", ":data", SIZE);
      SubclassReasoner2.main(null);
      new ExperimentClassifier().runCommMat("rdfs_example/lastfmDescHistH.txt", ":data", SIZE);
   }
   
   private void runRBC(String descFile, String context, String output) throws Exception {
      TrainConn = new VirtuosoConnection("jdbc:virtuoso://localhost:1113/charset=UTF-8/log_enable=2", "dba", "dba");
      TestConn = new VirtuosoConnection("jdbc:virtuoso://localhost:1115/charset=UTF-8/log_enable=2", "dba", "dba");

      BufferedWriter out = new BufferedWriter(new FileWriter(output));
      
      RDFDataDescriptor desc = RDFDataDescriptorParser.parse(descFile);
      setUpDataSource(desc, context);
      
      LDInstances trainInstances = new LDInstances();
      trainInstances.setDesc(desc);
      trainInstances.setDataSource(mTrainData);
      LDInstances testInstances = new LDInstances();
      testInstances.setDesc(desc);
      testInstances.setDataSource(mTestData);
   
      RBClassifier model = new RBClassifier();
      ConfusionMatrix mat = Evaluation.evaluateRBCModel(model, trainInstances, testInstances);
      
      out.write(mat.toString()); out.newLine();
      out.write("Total query size: " + Weigher.INSTANCE); out.newLine();
      out.write(Timer.INSTANCE.toString()); out.newLine();
      
      out.close();
   }
   
   private void runHRBC(String descFile, String context, String output) throws Exception {
      TrainConn = new VirtuosoConnection("jdbc:virtuoso://localhost:1113/charset=UTF-8/log_enable=2", "dba", "dba");
      TestConn = new VirtuosoConnection("jdbc:virtuoso://localhost:1115/charset=UTF-8/log_enable=2", "dba", "dba");

      BufferedWriter out = new BufferedWriter(new FileWriter(output));
      
      RDFDataDescriptor desc = RDFDataDescriptorParser.parse(descFile);
      setUpDataSource(desc, context);
      
      LDInstances trainInstances = new LDInstances();
      trainInstances.setDesc(desc);
      trainInstances.setDataSource(mTrainData);
      LDInstances testInstances = new LDInstances();
      testInstances.setDesc(desc);
      testInstances.setDataSource(mTestData);
   
      OntologyRBClassifier model = new OntologyRBClassifier(false);
      ConfusionMatrix mat = Evaluation.evaluateOntologyRBCModel(model, trainInstances, testInstances);
      
      out.write(mat.toString()); out.newLine();
      out.write("Total query size: " + Weigher.INSTANCE); out.newLine();
      out.write(Timer.INSTANCE.toString()); out.newLine();
      
      out.close();
   }
   
   private void runCut(String descFile, String context, String output, int trainPort, int testPort) throws Exception {
      RDFDataDescriptor desc = RDFDataDescriptorParser.parse(descFile);
      TrainConn = new VirtuosoConnection("jdbc:virtuoso://localhost:" + trainPort + "/charset=UTF-8/log_enable=2", "dba", "dba");
      TestConn = new VirtuosoConnection("jdbc:virtuoso://localhost:" + testPort + "/charset=UTF-8/log_enable=2", "dba", "dba");
      setUpDataSource(desc, context);
      
      BufferedWriter out = new BufferedWriter(new FileWriter(output));
      
      List<Cut> cuts = readCuts();
      for (int i = 0; i < cuts.size(); i++) {
         Cut cut = cuts.get(i);
         
         desc = RDFDataDescriptorParser.parse(descFile);
         setUpDataSource(desc, context);
         
         //run
         LDInstances trainInstances = new LDInstances();
         trainInstances.setDesc(desc);
         trainInstances.setDataSource(mTrainData);
         LDInstances testInstances = new LDInstances();
         testInstances.setDesc(desc);
         testInstances.setDataSource(mTestData);
      
         OntologyRBClassifier model = new OntologyRBClassifier(cut);
         ConfusionMatrix mat = Evaluation.evaluateOntologyRBCModel(model, trainInstances, testInstances);
         
         out.write("Cut size = " + cut.size()); out.newLine();
         out.write(mat.toString()); out.newLine();
         out.flush();
      }
      
      out.close();
   }
   
   private List<Cut> readCuts() throws IOException, RDFDatabaseException {
      List<Cut> allCuts = CollectionUtil.makeList();
      
      TBox tBox = mTrainData.getTBox();
      
      BufferedReader in = new BufferedReader(new FileReader("cut.txt"));
      
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

   private void runComm(String descFile, String context, int size) throws Exception {
      TrainConn = new VirtuosoConnection("jdbc:virtuoso://localhost:1113/charset=UTF-8/log_enable=2", "dba", "dba");

      BufferedWriter out = new BufferedWriter(new FileWriter("resultRaw" + size + ".txt"));
      
      LDInstances trainInstances = resetTrainingInstances(descFile, context);
      List<Cut> allCuts = readCuts();
      int[] cutSizes = new int[] {250, 3000};
      for (int i = 0; i < cutSizes.length; i++) {
         Cut cut = chooseCut(allCuts, cutSizes[i]);
         
         trainInstances = resetTrainingInstances(descFile, context);
         OntologyRBClassifier rbcOpt = new OntologyRBClassifier(cut);
         rbcOpt.setOptimizeOntology(true);
         RDFDatabaseConnectionFactory.QUERY_INFERENCE = false;
         rbcOpt.buildClassifier(trainInstances);
         out.write("RBC (cut " + cut.size() + ") optimized:"); out.newLine();
         out.write("Total query size: " + Weigher.INSTANCE); out.newLine();
         out.write(Timer.INSTANCE.toString()); out.newLine();

         trainInstances = resetTrainingInstances(descFile, context);
         OntologyRBClassifier rbcInf = new OntologyRBClassifier(cut);
         rbcInf.setOptimizeOntology(false);
         RDFDatabaseConnectionFactory.QUERY_INFERENCE = true;
         rbcInf.buildClassifier(trainInstances);
         out.write("RBC (cut " + cut.size() + ") online inf:"); out.newLine();
         out.write("Total query size: " + Weigher.INSTANCE); out.newLine();
         out.write(Timer.INSTANCE.toString()); out.newLine();
      }
      
      out.close();
   }
   
   private void runCommMat(String descFile, String context, int size) throws Exception {
      TrainConn = new VirtuosoConnection("jdbc:virtuoso://localhost:1113/charset=UTF-8/log_enable=2", "dba", "dba");

      BufferedWriter out = new BufferedWriter(new FileWriter("resultMat" + size + ".txt"));
      
      LDInstances trainInstances = resetTrainingInstances(descFile, context);
      List<Cut> allCuts = readCuts();
      int[] cutSizes = new int[] {250, 3000};
      for (int i = 0; i < cutSizes.length; i++) {
         Cut cut = chooseCut(allCuts, cutSizes[i]);
         
         trainInstances = resetTrainingInstances(descFile, context);
         OntologyRBClassifier rbcMat = new OntologyRBClassifier(cut);
         rbcMat.setOptimizeOntology(false);
         RDFDatabaseConnectionFactory.QUERY_INFERENCE = false;
         rbcMat.buildClassifier(trainInstances);
         out.write("RBC (cut " + cut.size() + ") materialized:"); out.newLine();
         out.write("Total query size: " + Weigher.INSTANCE); out.newLine();
         out.write(Timer.INSTANCE.toString()); out.newLine();
      }
      
      out.close();
   }
   
   private Cut chooseCut(List<Cut> cuts, int i) {
      for (Cut cut : cuts) {
         if (cut.size() >= i) {
            return cut;
         }
      }
      return null;
   }
   
   private LDInstances resetTrainingInstances(String descFile, String context) throws IOException, RDFDataDescriptorFormatException, RepositoryException {
      RDFDataDescriptor desc = RDFDataDescriptorParser.parse(descFile);
      setUpDataSource(desc, context);
      LDInstances trainInstances = new LDInstances();
      trainInstances.setDesc(desc);
      trainInstances.setDataSource(mTrainData);
   
      Weigher.INSTANCE.reset();
      Timer.INSTANCE.reset();
      return trainInstances;
   }
   
   private void setUpDataSource(RDFDataDescriptor desc, String graph) throws RepositoryException {
      mTrainData = new RDFDataSource(TrainConn, desc, graph);
      mTestData = new RDFDataSource(TestConn, desc, graph);
   }
   
}
