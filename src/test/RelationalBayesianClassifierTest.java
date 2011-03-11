package test;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import airldm2.classifiers.rl.AggregatedInstance;
import airldm2.classifiers.rl.InstanceAggregator;
import airldm2.classifiers.rl.RelationalBayesianClassifier;
import airldm2.core.LDInstances;
import airldm2.core.SSDataSource;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataDescriptorParser;
import airldm2.core.rl.RDFDataSource;

public class RelationalBayesianClassifierTest {
   private static final double EPSILON = 0.00001;
   
   @Before
   public void setUp() {
   }

   @Test
   public void testWithTrainInDBTestInDB() throws Exception {
      RDFDataDescriptor desc = RDFDataDescriptorParser.parse("rbc_example/moviesDesc.txt");
      //System.out.println(desc);
      
      //named RDF graph that stores all training triples 
      String trainGraph = ":train";
      SSDataSource trainSource = new RDFDataSource(trainGraph);
      LDInstances trainInstances = new LDInstances();
      trainInstances.setDesc(desc);
      trainInstances.setDataSource(trainSource);
   
      //named RDF graph that stores all test triples
      String testGraph = ":test";
      SSDataSource testSource = new RDFDataSource(testGraph);
      LDInstances testInstances = new LDInstances();
      testInstances.setDesc(desc);
      testInstances.setDataSource(testSource);
   
      RelationalBayesianClassifier rbc = new RelationalBayesianClassifier();
      rbc.buildClassifier(trainInstances);
      
      List<AggregatedInstance> aggregatedInstances = InstanceAggregator.aggregate(testInstances);
      double label = rbc.classifyInstance(aggregatedInstances.get(0));
      assertEquals(1.0, label, EPSILON);
   }

}
