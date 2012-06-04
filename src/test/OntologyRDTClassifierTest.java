package test;

import org.junit.Before;
import org.junit.Test;

import weka.classifiers.evaluation.ConfusionMatrix;
import airldm2.classifiers.Evaluation;
import airldm2.classifiers.rl.OntologyRDTClassifier;
import airldm2.core.LDInstances;
import airldm2.core.SSDataSource;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataDescriptorParser;
import airldm2.core.rl.RDFDataSource;
import airldm2.database.rdf.RDFDatabaseConnection;
import airldm2.database.rdf.RDFDatabaseConnectionFactory;

public class OntologyRDTClassifierTest {
   
   @Before
   public void setUp() {
   }
   
   @Test
   public void testFinancial() throws Exception {
      testWithTrainInDBTestInDB("rdfs_example/financialDesc.txt", ":financial", ":financial");
   }
   
   @Test
   public void testDisease() throws Exception {
      testWithTrainInDBTestInDB("rdfs_example/diseaseDesc.txt", "http://ehr", "http://ehr");
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
   
      OntologyRDTClassifier rdt = new OntologyRDTClassifier();
      
      ConfusionMatrix matrix = Evaluation.evaluateOntologyRDTModel(rdt, trainInstances, testInstances);
      System.out.println(matrix.toString("===Confusion Matrix==="));
      System.out.println("Accuracy = " + (1.0 - matrix.errorRate()));
   }
   
}
