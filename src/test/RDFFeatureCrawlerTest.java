package test;

import org.junit.Before;
import org.junit.Test;

import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataDescriptorParser;
import airldm2.core.rl.RDFDataSource;
import airldm2.database.rdf.RDFDatabaseConnection;
import airldm2.database.rdf.RDFDatabaseConnectionFactory;
import explore.RDFFeatureCrawler;

public class RDFFeatureCrawlerTest {

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
   }
      
   @Test
   public void testSmallOnFile() throws Exception {
      RDFDatabaseConnection conn = RDFDatabaseConnectionFactory.makeFromConfig();
      RDFDataSource source = new RDFDataSource(conn, ":small");
      RDFFeatureCrawler crawler = new RDFFeatureCrawler(source);
      crawler.crawl("rbc_example/smallDescEmpty.txt", "rbc_example/smallDescFilled.txt", 2);
   }
   
   @Test
   public void testHints() throws Exception {
      RDFDatabaseConnection conn = RDFDatabaseConnectionFactory.makeFromConfig();
      RDFDataSource source = new RDFDataSource(conn, ":hints");
      RDFFeatureCrawler crawler = new RDFFeatureCrawler(source);
      crawler.crawl("rbc_example/nci_hintsDescEmpty.txt", "rbc_example/nci_hintsDescFilled.txt", 0);
   }
   
   @Test
   public void testMovie() throws Exception {
      RDFDatabaseConnection conn = RDFDatabaseConnectionFactory.makeFromConfig();
      RDFDataSource source = new RDFDataSource(conn, ":default");
      RDFFeatureCrawler crawler = new RDFFeatureCrawler(source);
      crawler.crawl("rbc_example/moviesDescEmpty.txt", "rbc_example/moviesDescFilled.txt", 4);      
   }
   
}
