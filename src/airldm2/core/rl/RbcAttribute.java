package airldm2.core.rl;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openrdf.model.URI;

/**
 * 
 *  A place holder for an attributes of interest in Relational Bayesian Classifier
 *
 * @author neeraj (neeraj.kaul@gmail.com)
 * @since Jan 24, 2011
 * @version $Date: $
 */
public class RbcAttribute {
   /****
    * A vector of URI specifying relations starting with the targetType. 
    * The final URI has range as a data type (literal, numbers)
    * The initial URI is assumed to be targetType
    *  TODO : Currently not namespace aware
    */
   
   /**
    * Enum specifying how to aggregate values for a 1->n relationship
    */
   public enum ValueAggregator { NONE, HISTOGRAM, COUNT, AVG, MIN, MAX }
   
   private String mName;
   private ValueType mValueType;
   private ValueAggregator mAggregatorType;
   private URI mHierarchyRoot;
   private GraphPattern mGraph;
   
   public RbcAttribute(String name, ValueType valueType, ValueAggregator aggregatorType, URI hierarchyRoot, GraphPattern graph) {
      mName = name;
      mValueType = valueType;
      mAggregatorType = aggregatorType;
      mHierarchyRoot = hierarchyRoot;
      mGraph = graph;
   }
      
   public String getName() {
      return mName;
   }
   
   public GraphPattern getGraphPattern() {
      return mGraph;
   }

   public ValueAggregator getAggregatorType(){
      return mAggregatorType;
   }
   
   public URI getHierarchyRoot(){
      return mHierarchyRoot;
   }
   
   public ValueType getValueType(){
      return mValueType;
   }
   
   public void setValueType(ValueType v) {
      mValueType = v;
   }
   
   public int getDomainSize() {
      return getValueType().domainSize();
   }

   @Override
   public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
   }

   public void write(Writer out) throws IOException {
      out.write(RDFDataDescriptorParser.ATTRIBUTE);
      out.write(mName);
      out.write("\n");
      
      mValueType.write(out);
      out.write("\n");
      
      out.write(RDFDataDescriptorParser.AGGREGATOR);
      out.write(mAggregatorType.toString());
      out.write("\n");

      out.write(RDFDataDescriptorParser.HIERARCHY);
      out.write(mHierarchyRoot.stringValue());
      out.write("\n");
      
      out.write(mGraph.toString());
      out.write("\n");
   }

}