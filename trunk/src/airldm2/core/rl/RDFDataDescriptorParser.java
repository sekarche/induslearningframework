package airldm2.core.rl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

import airldm2.core.rl.RbcAttribute.ValueAggregator;
import airldm2.exceptions.RDFDataDescriptorFormatException;
import airldm2.util.CollectionUtil;
import airldm2.util.StringUtil;

public class RDFDataDescriptorParser {

   public static final String COMMENT_CHAR = "%";
   public static final String TARGET_TYPE = "@targetType ";
   public static final String TARGET = "@target ";
   public static final String ATTRIBUTE = "@attribute ";
   public static final String AGGREGATOR = "aggregator=";
   
   private static ValueFactory Factory = new ValueFactoryImpl();
   
   public static RDFDataDescriptor parse(String descFile) throws IOException, RDFDataDescriptorFormatException {
      URI targetType = null;
      String targetAttributeName = null;
      Map<String,RbcAttribute> attributes = new LinkedHashMap<String,RbcAttribute>();
      
      BufferedReader in = new BufferedReader(new FileReader(descFile));
      String line;
      while ((line = readContentLine(in)) != null) {
         if (line.startsWith(TARGET_TYPE)) {
            targetType = Factory.createURI(line.substring(TARGET_TYPE.length()));
         } else if (line.startsWith(TARGET)) {
            targetAttributeName = line.substring(TARGET.length());
         } else if (line.startsWith(ATTRIBUTE)) {
            String attributeName = line.substring(ATTRIBUTE.length()).trim();
            String propLine = readContentLine(in);
            String valueLine = readContentLine(in);
            String aggregatorLine = readContentLine(in);
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
   
   private static String readContentLine(BufferedReader in) throws IOException {
      String line;
      while ((line = in.readLine()) != null) {
         line = line.trim();
         if (line.startsWith(COMMENT_CHAR) || line.isEmpty()) 
            continue;
         else
            return line; 
      }
      
      return null;
   }

   private static RbcAttribute parseAttribute(String name, String propLine, String valueLine, String aggregatorLine) throws RDFDataDescriptorFormatException {
      String[] propStrs = StringUtil.trim(propLine.split(","));
      List<URI> props = CollectionUtil.makeList();
      for (String propStr : propStrs) {
         props.add(Factory.createURI(propStr));
      }
      
      if (!aggregatorLine.startsWith(AGGREGATOR))
         throw new RDFDataDescriptorFormatException("Aggregator is not defined properly: " + aggregatorLine);
      
      ValueAggregator aggregator = ValueAggregator.valueOf(aggregatorLine.substring(AGGREGATOR.length()).trim());
      
      ValueType valueType = null;
      String[] valueStrs = StringUtil.trim(valueLine.split("="));
      if (!"?".equals(valueStrs[1].trim())) {
         String[] possibleValues = StringUtil.trim(valueStrs[1].split(","));
         if (BinnedType.NAME.equalsIgnoreCase(valueStrs[0])) {
            double[] cutPoints = new double[possibleValues.length];
            for (int i = 0; i < possibleValues.length; i++) {
               cutPoints[i] = Double.parseDouble(possibleValues[i]);
            }
            valueType = new BinnedType(cutPoints);
            
            if (aggregator == ValueAggregator.INDEPENDENT_VAL) {
               throw new RDFDataDescriptorFormatException(ValueAggregator.INDEPENDENT_VAL + " must be a Nominal type.");
            }
         } else if (NominalType.NAME.equalsIgnoreCase(valueStrs[0])) {
            valueType = new NominalType(Arrays.asList(possibleValues));
            
            if (aggregator == ValueAggregator.AVG ||
                  aggregator == ValueAggregator.COUNT ||
                  aggregator == ValueAggregator.MAX ||
                  aggregator == ValueAggregator.MIN) {
               throw new RDFDataDescriptorFormatException("Aggregator " + aggregator + " can not be a Nominal type.");
            }
         } else if (EnumType.NAME.equalsIgnoreCase(valueStrs[0])) {
            URI[] possibleURIs = new URI[possibleValues.length];
            for (int i = 0; i < possibleValues.length; i++) {
               possibleURIs[i] = Factory.createURI(possibleValues[i]);
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
      }
      
      RbcAttribute attribute = new RbcAttribute(name, props, valueType, aggregator);
      
      return attribute;
   }
   
}
