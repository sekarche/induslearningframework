package test;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataDescriptorParser;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.core.rl.ValueType;
import airldm2.database.rdf.RDFDatabaseConnection;
import airldm2.database.rdf.RDFDatabaseConnectionFactory;
import airldm2.database.rdf.VirtuosoConnection;
import explore.RDFDataDescriptorEnhancer;

public class RDFDataDescriptorEnhancerTest {

   @Before
   public void setUp() {
   }
   
   @Test
   public void testSmall() throws Exception {
      RDFDataDescriptor desc = RDFDataDescriptorParser.parse("rbc_example/smallDescIncomplete.txt");
      RbcAttribute label = desc.getTargetAttribute();
      RbcAttribute ind = desc.getNonTargetAttributeList().get(0);
      Assert.assertNull(label.getValueType());
      Assert.assertNull(ind.getValueType());
      
      RDFDatabaseConnection conn = RDFDatabaseConnectionFactory.makeFromConfig();
      RDFDataSource source = new RDFDataSource(conn, ":small");
      RDFDataDescriptorEnhancer enhancer = new RDFDataDescriptorEnhancer(source);
      enhancer.fillDomain(desc);
      
      ValueType labelValues = label.getValueType();
      Assert.assertEquals(Arrays.asList(new String[] {"A", "B", "C"}), labelValues.getStringValues());
      ValueType indValues = ind.getValueType();
      Assert.assertEquals(Arrays.asList(new String[] {"i1", "i2", "i3"}), indValues.getStringValues());
   }
   
   @Test
   public void testSmallOnFile() throws Exception {
      RDFDatabaseConnection conn = RDFDatabaseConnectionFactory.makeFromConfig();
      RDFDataSource source = new RDFDataSource(conn, ":small");
      RDFDataDescriptorEnhancer enhancer = new RDFDataDescriptorEnhancer(source);
      enhancer.fillDomain("rbc_example/smallDescIncomplete.txt", "rbc_example/smallDescFilled.txt");
   }
   
   //Connects to a remote SPARQL - turn on only when needed
   //@Test
   public void testNCIHintsOnFile() throws Exception {
      final String HINTS_DESC = "rbc_example/nci_hintsDescIncomplete.txt";
      final String HINTS_DESC_FILLED = "rbc_example/nci_hintsDesc.txt";
      final String LOGD_SPARQL = "http://logd.tw.rpi.edu/sparql";
      
      RDFDatabaseConnection conn = new VirtuosoConnection(LOGD_SPARQL);
      RDFDataSource source = new RDFDataSource(conn);
      RDFDataDescriptorEnhancer enhancer = new RDFDataDescriptorEnhancer(source);
      enhancer.fillDomain(HINTS_DESC, HINTS_DESC_FILLED);
   }
   
}
