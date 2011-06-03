package explore;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.openrdf.model.URI;

import airldm2.core.rl.PropertyChain;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataDescriptorParser;
import airldm2.core.rl.RDFDataSource;
import airldm2.exceptions.RDFDataDescriptorFormatException;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.util.CollectionUtil;

public class RDFFeatureCrawler {

   private RDFDataSource mDataSource;

   public RDFFeatureCrawler(RDFDataSource dataSource) {
      mDataSource = dataSource;
   }

   public void crawl(String inDescFile, String outDescFile, int maxDepth) throws IOException, RDFDataDescriptorFormatException, RDFDatabaseException {
      RDFDataDescriptor desc = RDFDataDescriptorParser.parse(inDescFile);
      crawl(desc, maxDepth);
      BufferedWriter out = new BufferedWriter(new FileWriter(outDescFile));
      desc.write(out);
      out.close();
   }
   
   public void crawl(RDFDataDescriptor desc, int maxDepth) throws RDFDatabaseException {
      List<PropertyChain> allPropertyChains = CollectionUtil.makeList();
      List<PropertyChain> currentDepth = crawlNextDepth(desc, new PropertyChain());
      allPropertyChains.addAll(currentDepth);
      for (int i = 0; i < maxDepth; i++) {
         for (PropertyChain c : currentDepth) {
            currentDepth = crawlNextDepth(desc, c);
         }
         allPropertyChains.addAll(currentDepth);
      }      
      
      allPropertyChains.remove(desc.getTargetAttribute().getProperties());
      
      
      //desc.addNonTargetAttributes(allAttributes);
   }
   
   private List<PropertyChain> crawlNextDepth(RDFDataDescriptor desc, PropertyChain propChain) throws RDFDatabaseException {
      List<URI> props = mDataSource.getPropertiesOf(desc.getTargetType(), propChain);
      List<PropertyChain> nextDepth = CollectionUtil.makeList();
      for (URI prop : props) {
         nextDepth.add(propChain.append(prop));
      }
      return nextDepth;
   }
   
}
