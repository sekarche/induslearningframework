package airldm2.core.rl;
import java.net.URI;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

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
   public enum ValueAggregator { NONE, INDEPENDENT_VAL, COUNT, MODE, AVG, MIN, MAX }
   
   //TODO: this may be promoted into classes (carrying different types of possible values)
   /**
    * Enum specifying what is the datatype of the target value to be predicted
    */
   public enum ValueType { NOMINAL, ENUMERATED, BINNED }
   
   private List<URI> mProperties;
   private ValueType mValueType;
   private ValueAggregator mAggregatorType;
   
   /*The bins to use if value type is BINNED*/
   private BinnedType mBins;
   /*possible values if value type is not BINNED*/
   private List<String> mPossibleValues;
   
   public RbcAttribute(List<URI> props, ValueType valueType, ValueAggregator aggregatorType) {
      mProperties = props;
      mValueType = valueType;
      mAggregatorType = aggregatorType;
   }
      
   public ValueAggregator getAggregatoraType(){
      return mAggregatorType;
   }
   
   public ValueType getValueType(){
      return mValueType;
   }

   public void setBins(BinnedType bins) {
      mBins = bins;
   }

   public BinnedType getBins() {
      return mBins;
   }

   public void setPossibleValues(List<String> possibleValues) {
      mPossibleValues = possibleValues;
   }

   public List<String> getPossibleValues() {
      return mPossibleValues;
   }
   
   @Override
   public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
   }
   
}