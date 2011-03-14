package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
      String trainGraph = ":moviesTrain";
      SSDataSource trainSource = new RDFDataSource(trainGraph);
      LDInstances trainInstances = new LDInstances();
      trainInstances.setDesc(desc);
      trainInstances.setDataSource(trainSource);
   
      //named RDF graph that stores all test triples
      String testGraph = ":moviesTest";
      SSDataSource testSource = new RDFDataSource(testGraph);
      LDInstances testInstances = new LDInstances();
      testInstances.setDesc(desc);
      testInstances.setDataSource(testSource);
   
      RelationalBayesianClassifier rbc = new RelationalBayesianClassifier();
      rbc.buildClassifier(trainInstances);
      
      List<AggregatedInstance> aggregatedInstances = InstanceAggregator.aggregate(testInstances);
      for (int i = 0; i < aggregatedInstances.size(); i++) {
         AggregatedInstance instance = aggregatedInstances.get(i);
         //double label = rbc.classifyInstance(instance);
         assertTrue(instance.getLabel() >= 0);
      }
   }

}
