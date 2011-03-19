package airldm2.core.rl;
import java.util.List;

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
   public enum ValueAggregator { NONE, INDEPENDENT_VAL, COUNT, AVG, MIN, MAX }
   
   private String mName;
   private List<URI> mProperties;
   private ValueType mValueType;
   private ValueAggregator mAggregatorType;
   
   public RbcAttribute(String name, List<URI> props, ValueType valueType, ValueAggregator aggregatorType) {
      mName = name;
      mProperties = props;
      mValueType = valueType;
      mAggregatorType = aggregatorType;
   }
      
   public String getName() {
      return mName;
   }
   
   public List<URI> getProperties() {
      return mProperties;
   }

   public ValueAggregator getAggregatorType(){
      return mAggregatorType;
   }
   
   public ValueType getValueType(){
      return mValueType;
   }
   
   @Override
   public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
   }

}