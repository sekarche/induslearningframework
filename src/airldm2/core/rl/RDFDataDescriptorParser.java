package airldm2.core.rl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import airldm2.core.rl.RbcAttribute.ValueAggregator;
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
            RbcAttribute attribute = parseAttribute(attributeName, propLine, valueLine, aggregatorLine);
            attributes.put(attributeName, attribute);
         }
      }
      in.close();
      
      if (!attributes.containsKey(targetAttributeName)) {
         throw new RDFDataDescriptorFormatException("Target " + targetAttributeName + " is not defined.");
      }
      
      RbcAttribute targetAttribute = attributes.get(targetAttributeName);
      if (targetAttribute.getAggregatorType() != ValueAggregator.NONE) {
         throw new UnsupportedOperationException("Aggregator on the target attribute is not supported in this version.");
      }
      
      return new RDFDataDescriptor(targetType, targetAttributeName, attributes);
   }

   private static RbcAttribute parseAttribute(String name, String propLine, String valueLine, String aggregatorLine) throws RDFDataDescriptorFormatException {
      String[] propStrs = propLine.split(",");
      List<URI> props = CollectionUtil.makeList();
      for (String propStr : propStrs) {
         props.add(URI.create(propStr.trim()));
      }
      
      if (!aggregatorLine.startsWith(AGGREGATOR))
         throw new RDFDataDescriptorFormatException("Aggregator is not defined properly: " + aggregatorLine);
      
      ValueAggregator aggregator = ValueAggregator.valueOf(aggregatorLine.substring(AGGREGATOR.length()).trim());
      
      String[] valueStrs = valueLine.split("=");
      String[] possibleValues = valueStrs[1].split(",");
      ValueType valueType = null;
      if ("BINNED".equalsIgnoreCase(valueStrs[0])) {
         double[] cutPoints = new double[possibleValues.length];
         for (int i = 0; i < possibleValues.length; i++) {
            cutPoints[i] = Double.parseDouble(possibleValues[i]);
         }
         valueType = new BinnedType(cutPoints);
         
         if (aggregator == ValueAggregator.INDEPENDENT_VAL) {
            throw new RDFDataDescriptorFormatException(ValueAggregator.INDEPENDENT_VAL + " must be a Nominal type.");
         }
      } else if ("NOMINAL".equalsIgnoreCase(valueStrs[0])) {
         valueType = new NominalType(Arrays.asList(possibleValues));
         
         if (aggregator == ValueAggregator.AVG ||
               aggregator == ValueAggregator.COUNT ||
               aggregator == ValueAggregator.MAX ||
               aggregator == ValueAggregator.MIN) {
            throw new RDFDataDescriptorFormatException("Aggregator " + aggregator + " can not be a Nominal type.");
         }
      } else if ("ENUM".equalsIgnoreCase(valueStrs[0])) {
         URI[] possibleURIs = new URI[possibleValues.length];
         for (int i = 0; i < possibleValues.length; i++) {
            possibleURIs[i] = URI.create(possibleValues[i]);
         }
         
         valueType = new EnumType(Arrays.asList(possibleURIs));
         
         if (aggregator == ValueAggregator.AVG ||
               aggregator == ValueAggregator.COUNT ||
               aggregator == ValueAggregator.MAX ||
               aggregator == ValueAggregator.MIN) {
            throw new RDFDataDescriptorFormatException("Aggregator " + aggregator + " can not be an Enum type.");
         }
      } else {
         throw new RDFDataDescriptorFormatException("Value type " + valueStrs[0] + " is not supported.");
      }
      
      RbcAttribute attribute = new RbcAttribute(name, props, valueType, aggregator);
      
      return attribute;
   }
   
}
