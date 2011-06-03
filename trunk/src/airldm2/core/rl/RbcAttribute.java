package airldm2.core.rl;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openrdf.model.URI;

import airldm2.util.CollectionUtil;
import airldm2.util.StringUtil;

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
   public enum ValueAggregator { NONE, INDEPENDENT_VAL, COUNT, AVG, MIN, MAX }
   
   private String mName;
   private PropertyChain mPropertyChain;
   private ValueType mValueType;
   private ValueAggregator mAggregatorType;
   
   public RbcAttribute(String name, PropertyChain props, ValueType valueType, ValueAggregator aggregatorType) {
      mName = name;
      mPropertyChain = props;
      mValueType = valueType;
      mAggregatorType = aggregatorType;
   }
      
   public String getName() {
      return mName;
   }
   
   public PropertyChain getProperties() {
      return mPropertyChain;
   }

   public ValueAggregator getAggregatorType(){
      return mAggregatorType;
   }
   
   public ValueType getValueType(){
      return mValueType;
   }
   
   public void setValueType(ValueType v) {
      mValueType = v;
   }

   @Override
   public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
   }

   public void write(Writer out) throws IOException {
      out.write(RDFDataDescriptorParser.ATTRIBUTE);
      out.write(mName);
      out.write("\n");
      
      out.write(StringUtil.toCSV(CollectionUtil.toStringList(mPropertyChain.getList())));
      out.write("\n");
      
      mValueType.write(out);
      out.write("\n");
      
      out.write(RDFDataDescriptorParser.AGGREGATOR);
      out.write(mAggregatorType.toString());
      out.write("\n");
   }

}