package airldm2.core.rl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import airldm2.exceptions.RDFDataDescriptorFormatException;
import airldm2.util.CollectionUtil;

public class RDFDataDescriptorParser {

   private static final String COMMENT_CHAR = "%";
   private static final String TARGET_TYPE = "@targetType ";
   private static final String TARGET = "@target ";
   private static final String ATTRIBUTE = "@attribute ";
   private static final String AGGREGATOR = "aggregator=";
   
   public static RDFDataDescriptor parse(String descFile) throws IOException, RDFDataDescriptorFormatException {
      URI targetType = null;
      String targetAttributeName = null;
      Map<String,RbcAttribute> attributes = CollectionUtil.makeMap();
      
      BufferedReader in = new BufferedReader(new FileReader(descFile));
      String line;
      while ((line = in.readLine()) != null) {
         line = line.trim();
         if (line.startsWith(COMMENT_CHAR) || line.isEmpty()) 
            continue;

         if (line.startsWith(TARGET_TYPE)) {
            targetType = URI.create(line.substring(TARGET_TYPE.length()));
         } else if (line.startsWith(TARGET)) {
            targetAttributeName = line.substring(TARGET.length());
         } else if (line.startsWith(ATTRIBUTE)) {
            String attributeName = line.substring(ATTRIBUTE.length());
            String propChain = in.readLine();
            String valueType = in.readLine();
            String aggregatorType = in.readLine();
            RbcAttribute attribute = parseAttribute(propChain, valueType, aggregatorType);
            attributes.put(attributeName, attribute);
         }
      }
      in.close();
      
      if (!attributes.containsKey(targetAttributeName)) {
         throw new RDFDataDescriptorFormatException("Target " + targetAttributeName + " is not defined.");
      }
      
      return new RDFDataDescriptor(targetType, targetAttributeName, attributes);
   }

   private static RbcAttribute parseAttribute(String propChain, String valueType, String aggregatorType) {
//      http://data.linkedmdb.org/resource/movie/film/actor,http://xmlns.com/foaf/0.1/page,http://rdf.freebase.com/ns/people.person.birth_year
//         BINNED=MIN-1940,1940-60,1960_MAX
//         aggregator=AVG
      
      
      
      return null;//new RbcAttribute();
   }
   
}
