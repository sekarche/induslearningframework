package test;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;

import airldm2.classifiers.rl.RelationalBayesianClassifier;
import airldm2.core.LDInstances;
import airldm2.core.SSDataSource;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataDescriptorParser;
import airldm2.core.rl.RDFDataSource;
import airldm2.database.rdf.SuffStatQueryConstructor;
import airldm2.database.rdf.SuffStatQueryParameter;

public class RelationalBayesianClassifierTest {
   private static final double EPSILON = 0.00001;
   
   @Before
   public void setUp() {
   }

   @Test
   public void testWithTrainInDBTestInDB() throws Exception {
      RDFDataDescriptor desc = RDFDataDescriptorParser.parse("rbc_example/moviesDesc.txt");
      //System.out.println(desc);
      
      String trainContext = ":train";
      SSDataSource trainSource = new RDFDataSource(trainContext);
      LDInstances trainInstances = new LDInstances();
      trainInstances.setDesc(desc);
      trainInstances.setDataSource(trainSource);
   
      String testContext = ":test";
      SSDataSource testSource = new RDFDataSource(testContext);
      LDInstances testInstances = new LDInstances();
      testInstances.setDesc(desc);
      testInstances.setDataSource(testSource);
   
      RelationalBayesianClassifier rbc = new RelationalBayesianClassifier();
      rbc.buildClassifier(trainInstances);
      URI test1 = URI.create("http://data.linkedmdb.org/resource/film/2723");
      double label = rbc.classifyInstance(testInstances, test1);
      assertEquals(1.0, label, EPSILON);
   }

}
