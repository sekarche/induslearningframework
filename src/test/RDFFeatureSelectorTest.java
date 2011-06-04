package test;

import org.junit.Before;
import org.junit.Test;

import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataDescriptorParser;
import airldm2.core.rl.RDFDataSource;
import airldm2.database.rdf.RDFDatabaseConnection;
import airldm2.database.rdf.RDFDatabaseConnectionFactory;
import explore.RDFFeatureCrawler;
import explore.RDFFeatureSelector;

public class RDFFeatureSelectorTest {

   @Before
   public void setUp() {
   }
   
   @Test
   public void testSmall() throws Exception {
      RDFDataDescriptor desc = RDFDataDescriptorParser.parse("rbc_example/smallDescEmpty.txt");
      
      RDFDatabaseConnection conn = RDFDatabaseConnectionFactory.makeFromConfig();
      RDFDataSource source = new RDFDataSource(conn, ":small");
      RDFFeatureCrawler crawler = new RDFFeatureCrawler(source);
      crawler.crawl(desc, 2);    
      
      RDFFeatureSelector selector = new RDFFeatureSelector(source);
      selector.select(desc, 5);
   }

}
