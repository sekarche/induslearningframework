package airldm2.classifiers.rl;

import java.io.FileWriter;
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriter;
import org.openrdf.sail.memory.MemoryStore;

import weka.core.Utils;
import airldm2.core.LDInstances;
import airldm2.core.SSDataSource;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataDescriptorParser;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.database.rdf.RDFDatabaseConnection;
import airldm2.database.rdf.VirtuosoConnection;

public class RemoteRBCClassifier {

   private static ValueFactory ValueFac = new ValueFactoryImpl();
   
   public static void main(String[] args) throws Exception {
      String descFile = Utils.getOption("desc", args);
      String outputFile = Utils.getOption("output", args);
      String trainEndpoint = Utils.getOption("trainEndpoint", args);
      String trainGraph = Utils.getOption("trainGraph", args);
      String testEndpoint = Utils.getOption("testEndpoint", args);
      String testGraph = Utils.getOption("testGraph", args);
      
      if ("".equals(trainGraph)) {
         trainGraph = null;
      }
      
      if ("".equals(testGraph)) {
         testGraph = null;
      }
      
      if (descFile == null || "".equals(descFile)
            || outputFile == null || "".equals(outputFile)
            || trainEndpoint == null || "".equals(trainEndpoint)
            || testEndpoint == null || "".equals(testEndpoint)) {
         printUsage();
         System.exit(0);
      }
      
      run(descFile, outputFile, trainEndpoint, trainGraph, testEndpoint, testGraph);
   }

   private static void printUsage() {
      System.out.println("RemoteRBCClassifier - Learning and classifying remote RDF data");
      System.out.println();
      System.out.println("Usage:");
      System.out.println("   -desc FILE");
      System.out.println("      Descriptor of training and test RDF graphs");
      System.out.println("   -output FILE");
      System.out.println("      Output of classifications in RDF/XML format");
      System.out.println("   -trainEndpoint URI");
      System.out.println("      SPARQL endpoint holding training RDF data");
      System.out.println("   -trainGraph URI");
      System.out.println("      [OPTIONAL] Context URI specifying the named RDF graph of training instances");
      System.out.println("   -testEndpoint URI");
      System.out.println("      SPARQL endpoint holding test RDF data");
      System.out.println("   -testGraph URI");
      System.out.println("      [OPTIONAL] Context URI specifying the named RDF graph of test instances");
   }
   
   public static void run(String descFile, String outputFile, String trainEndpoint, String trainGraph, String testEndpoint, String testGraph) throws Exception {
      RDFDataDescriptor desc = RDFDataDescriptorParser.parse(descFile);
      String[] classLabels = desc.getClassLabels();
      
      //named RDF graph that stores all training triples 
      RDFDatabaseConnection trainConn = new VirtuosoConnection(trainEndpoint);
      SSDataSource trainSource = new RDFDataSource(trainConn, trainGraph);
      LDInstances trainInstances = new LDInstances();
      trainInstances.setDesc(desc);
      trainInstances.setDataSource(trainSource);
      
      //named RDF graph that stores all training triples 
      RDFDatabaseConnection testConn = new VirtuosoConnection(testEndpoint);
      SSDataSource testSource = new RDFDataSource(testConn, testGraph);
      LDInstances testInstances = new LDInstances();
      testInstances.setDesc(desc);
      testInstances.setDataSource(testSource);
      
      //Build RBC
      RelationalBayesianClassifier rbc = new RelationalBayesianClassifier();
      rbc.buildClassifier(trainInstances);
      
      //Classify
      Repository repository = new SailRepository(new MemoryStore());
      repository.initialize();
      RepositoryConnection conn = repository.getConnection();
      
      RbcAttribute targetAttribute = desc.getTargetAttribute();
      AggregatedInstances aggregatedInstances = InstanceAggregator.aggregateAll(testInstances);
      for (AggregatedInstance i : aggregatedInstances.getInstances()) {
         URI uri = i.getURI();
         int classification = (int) rbc.classifyInstance(i);
         
         addClassification(conn, uri, targetAttribute, classLabels[classification]);
      }
      
      FileWriter rdfWriter = new FileWriter(outputFile);
      conn.export(new RDFXMLPrettyWriter(rdfWriter));
      conn.close();
      repository.shutDown();
   }

   private static void addClassification(RepositoryConnection conn, URI subject, RbcAttribute targetAttribute, String value) throws RepositoryException {
      List<URI> props = targetAttribute.getPropertyChain().getList();
      if (props.size() > 1) {
         throw new UnsupportedOperationException("Target attribute chain has more than one properties.");
      }
      
      URI prop = props.get(0);
      conn.add(subject, prop, ValueFac.createLiteral(value));
   }
   
}
