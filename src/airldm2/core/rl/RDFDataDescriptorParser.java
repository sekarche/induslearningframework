package airldm2.core.rl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

import airldm2.core.rl.NumericType.Distribution;
import airldm2.exceptions.RDFDataDescriptorFormatException;
import airldm2.util.StringUtil;

public class RDFDataDescriptorParser {

   public static final String COMMENT_CHAR = "%";
   public static final String INSTANCE_VAR = "@instance_var ";
   public static final String VALUE_VAR = "@value_var ";
   public static final String HIERARCHY_VAR = "@hierarchy_var ";
   public static final String TARGET_TYPE = "@targetType ";
   public static final String TARGET = "@target ";
   public static final String ATTRIBUTE = "@attribute ";
   public static final String AGGREGATOR = "aggregator=";
   public static final String ESTIMATOR = "numbericEstimator=";
   public static final String HIERARCHY = "hierarchyRoot=";
   public static final String BINNED = "BINNED:";
   
   private static ValueFactory Factory = new ValueFactoryImpl();
   private static String InstanceVar;
   private static String ValueVar;
   private static String HierarchyVar;
   
   public static RDFDataDescriptor parse(String descFile) throws IOException, RDFDataDescriptorFormatException {
      InstanceVar = null;
      ValueVar = null;
      HierarchyVar = null;
      
      URI targetType = null;
      String targetAttributeName = null;
      Map<String,RbcAttribute> attributes = new LinkedHashMap<String,RbcAttribute>();
      
      BufferedReader in = new BufferedReader(new FileReader(descFile));
      String line;
      while ((line = readContentLine(in)) != null) {
         if (line.startsWith(INSTANCE_VAR)) {
            InstanceVar = line.substring(INSTANCE_VAR.length());
         } else if (line.startsWith(VALUE_VAR)) {
            ValueVar = line.substring(VALUE_VAR.length());
         } else if (line.startsWith(HIERARCHY_VAR)) {
            HierarchyVar = line.substring(HIERARCHY_VAR.length());
         } else if (line.startsWith(TARGET_TYPE)) {
            targetType = Factory.createURI(line.substring(TARGET_TYPE.length()));
         } else if (line.startsWith(TARGET)) {
            targetAttributeName = line.substring(TARGET.length());
         } else if (line.startsWith(ATTRIBUTE)) {
            String graphPattern;
            RbcAttribute attribute;
            String attributeName = line.substring(ATTRIBUTE.length()).trim();
            String valueLine = readContentLine(in);
            String aggregatorLine = readContentLine(in);
            String estimatorLine = readContentLine(in);
            if ("{".equals(estimatorLine)) {
               graphPattern = readContentLines(in, "}");
               attribute = parseAttribute(attributeName, valueLine, aggregatorLine, null, null, graphPattern);
               attributes.put(attributeName, attribute);
               continue;
            }
            
            String hierarchyLine = readContentLine(in);
            if ("{".equals(hierarchyLine)) {
               graphPattern = readContentLines(in, "}");
               attribute = parseAttribute(attributeName, valueLine, aggregatorLine, estimatorLine, null, graphPattern);
               attributes.put(attributeName, attribute);
               continue;
            }
            
            graphPattern = readContentLines(in, "}");
            attribute = parseAttribute(attributeName, valueLine, aggregatorLine, estimatorLine, hierarchyLine, graphPattern);
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
      
      return new RDFDataDescriptor(InstanceVar, ValueVar, HierarchyVar, targetType, targetAttributeName, attributes);
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
   
   private static String readContentLines(BufferedReader in, String endLine) throws IOException {
      StringBuilder content = new StringBuilder();
      String line;
      while ((line = in.readLine()) != null) {
         line = line.trim();
         if (line.startsWith(COMMENT_CHAR) || line.isEmpty()) 
            continue;
         else if (line.equals(endLine))
            return content.toString();
         else {
            content.append(line);
            content.append(" ");
         }
      }
      
      return null;
   }

   private static RbcAttribute parseAttribute(String name, String valueLine, String aggregatorLine, String estimatorLine, String hierarchyLine, String graphPatternStr) throws RDFDataDescriptorFormatException {
      if (!aggregatorLine.startsWith(AGGREGATOR))
         throw new RDFDataDescriptorFormatException("Aggregator is not defined properly: " + aggregatorLine);
      
      String rawValueType = null;
      String[] possibleValues = null;
      ValueAggregator aggregator = ValueAggregator.valueOf(aggregatorLine.substring(AGGREGATOR.length()).trim());
      Distribution dist = null;
      double[] cutPoints = null;
      
      String[] valueStrs = StringUtil.trim(valueLine.split("="));
      rawValueType = valueStrs[0];
      if (valueStrs.length > 1 && !"".equals(valueStrs[1].trim()) && !"?".equals(valueStrs[1].trim())) {
         possibleValues = StringUtil.trim(valueStrs[1].split(","));
      }
      
      if (estimatorLine != null) {
         String estimator = estimatorLine.substring(ESTIMATOR.length()).trim();
         if (estimator.startsWith(BINNED)) {
            estimator = estimator.substring(BINNED.length());
            String[] cutPointStrs = StringUtil.trim(estimator.split(","));
            cutPoints = new double[cutPointStrs.length];
            for (int i = 0; i < cutPointStrs.length; i++) {
               cutPoints[i] = Double.parseDouble(cutPointStrs[i]);
            }
         } else {
            dist = Distribution.valueOf(estimator);
         }
      }
      
      ValueType valueType = null;
      if (valueStrs.length == 1 || !"?".equals(valueStrs[1].trim())) {
         if (ValueAggregator.isNumericOutput(aggregator)) {
            if (estimatorLine == null) {
               throw new RDFDataDescriptorFormatException(name + ": Numberic estimator must be specified on a Numeric type or a numerical aggregator.");
            } else if (aggregator != ValueAggregator.COUNT && !NumericType.NAME.equalsIgnoreCase(rawValueType)) {
               throw new RDFDataDescriptorFormatException(name + ": Numberic aggregator can not be applied on a non-numeric type.");
            }
            
            if (dist == null) {
               valueType = new BinnedType(cutPoints);
            } else {
               valueType = new NumericType(dist);
            }
            
         } else {
            if (NominalType.NAME.equalsIgnoreCase(rawValueType)) {
               if (estimatorLine != null) {
                  throw new RDFDataDescriptorFormatException(name + ": Numberic estimator can not be applied on a Nominal type.");
               }
               
               valueType = new NominalType(Arrays.asList(possibleValues));
               
            } else if (EnumType.NAME.equalsIgnoreCase(rawValueType)) {
               if (estimatorLine != null) {
                  throw new RDFDataDescriptorFormatException(name + ": Numberic estimator can not be applied on a Nominal type.");
               }
               
               URI[] possibleURIs = new URI[possibleValues.length];
               for (int i = 0; i < possibleValues.length; i++) {
                  possibleURIs[i] = Factory.createURI(possibleValues[i]);
               }
               
               valueType = new EnumType(Arrays.asList(possibleURIs));
               
            } else if (NumericType.NAME.equalsIgnoreCase(rawValueType)) {
               if (aggregator == ValueAggregator.HISTOGRAM) {
                  throw new RDFDataDescriptorFormatException(name + ": " + ValueAggregator.HISTOGRAM + " can not be applied on a Numeric type.");
               } else if (estimatorLine == null) {
                  throw new RDFDataDescriptorFormatException(name + ": Numberic estimator must be specified on a Numeric type.");
               }
               
               if (dist == null) {
                  valueType = new BinnedType(cutPoints);
               } else {
                  valueType = new NumericType(dist);
               }
            }
         }
      }
            
      URI hierarchyRootURI = null;
      if (hierarchyLine != null) {
         String hierarchy = hierarchyLine.substring(HIERARCHY.length()).trim();
         if (!hierarchy.isEmpty()) {
            hierarchyRootURI = Factory.createURI(hierarchy);
         }
      }

      graphPatternStr = StringUtil.appendAllVarsWith(graphPatternStr, name);
      GraphPattern graphPattern = new GraphPattern(InstanceVar + name, ValueVar + name, HierarchyVar + name, graphPatternStr);
      
      RbcAttribute attribute = new RbcAttribute(name, valueType, aggregator, hierarchyRootURI, graphPattern);
      return attribute;
   }
   
}
