package test;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;

import airldm2.core.rl.RDFDataSource;
import airldm2.database.rdf.RDFDatabaseConnection;
import airldm2.database.rdf.RDFDatabaseConnectionFactory;
import explore.RDFGuidedFeatureCrawler;
import explore.tree.BFS;
import explore.tree.BestScore;

public class RDFGuidedFeatureCrawlerTest {

   @Before
   public void setUp() {
   }
   
   @Test
   public void testMovie() throws Exception {
      RDFDatabaseConnection conn = RDFDatabaseConnectionFactory.makeFromConfig();
      RDFDataSource source = new RDFDataSource(conn, ":default");
      RDFGuidedFeatureCrawler crawler = new RDFGuidedFeatureCrawler(source);
      crawler.crawl("rbc_example/moviesDescEmpty.txt", "rbc_example/moviesDescFilled_D.txt", 150, 30, new BFS());
      crawler.crawl("rbc_example/moviesDescEmpty.txt", "rbc_example/moviesDescFilled_I.txt", 150, 30, new BestScore());
   }

   @Test
   public void testCensus() throws Exception {
      RDFDatabaseConnection conn = RDFDatabaseConnectionFactory.makeFromConfig();
      RDFDataSource source = new RDFDataSource(conn, ":census");
      RDFGuidedFeatureCrawler crawler = new RDFGuidedFeatureCrawler(source);
      crawler.setExclusion(new URI[] {new ValueFactoryImpl().createURI("http://logd.tw.rpi.edu/source/data-gov/dataset/311/vocab/")});
      crawler.crawl("rbc_example/censusDescEmpty.txt", "rbc_example/censusDescFilled_D.txt", 40, 40, new BFS());
      crawler.crawl("rbc_example/censusDescEmpty.txt", "rbc_example/censusDescFilled_I.txt", 40, 40, new BestScore());
   }
   
}
