package airldm2.core.rl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import airldm2.core.rl.RbcAttribute.ValueAggregator;
import airldm2.core.rl.RbcAttribute.ValueType;
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
            String attributeName = line.substring(ATTRIBUTE.length()).trim();
            String propLine = in.readLine().trim();
            String valueLine = in.readLine().trim();
            String aggregatorLine = in.readLine().trim();
            RbcAttribute attribute = parseAttribute(propLine, valueLine, aggregatorLine);
            attributes.put(attributeName, attribute);
         }
      }
      in.close();
      
      if (!attributes.containsKey(targetAttributeName)) {
         throw new RDFDataDescriptorFormatException("Target " + targetAttributeName + " is not defined.");
      }
      
      return new RDFDataDescriptor(targetType, targetAttributeName, attributes);
   }

   private static RbcAttribute parseAttribute(String propLine, String valueLine, String aggregatorLine) throws RDFDataDescriptorFormatException {
      String[] propStrs = propLine.split(",");
      List<URI> props = CollectionUtil.makeList();
      for (String propStr : propStrs) {
         props.add(URI.create(propStr.trim()));
      }
      
      if (!aggregatorLine.startsWith(AGGREGATOR))
         throw new RDFDataDescriptorFormatException("Aggregator is not defined properly: " + aggregatorLine);
      
      ValueAggregator aggregator = ValueAggregator.valueOf(aggregatorLine.substring(AGGREGATOR.length()).trim());
      
      String[] valueStrs = valueLine.split("=");
      ValueType valueType = ValueType.valueOf(valueStrs[0]);
      String[] possibleValues = valueStrs[1].split(",");
      
      RbcAttribute attribute = new RbcAttribute(props, valueType, aggregator);
      if (valueType == ValueType.BINNED) {
         double[] cutPoints = new double[possibleValues.length];
         for (int i = 0; i < possibleValues.length; i++) {
            cutPoints[i] = Double.parseDouble(possibleValues[i]);
         }
         BinnedType bins = new BinnedType(cutPoints);
         attribute.setBins(bins);
      } else {
         attribute.setPossibleValues(Arrays.asList(possibleValues));
      }
      
      return attribute;
   }
   
}
