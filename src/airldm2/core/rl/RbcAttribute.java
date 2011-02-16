package airldm2.core.rl;
import java.net.URI;
import java.util.List;

/**
 * 
 *  A place holder for an attributes of interest in Relational Bayesian Classifier
 *
 * @author neeraj (neeraj.kaul@gmail.com)
 * @since Jan 24, 2011
 * @version $Date: $
 */
public class RbcAttribute {
   /* http://data.linkedmdb.org/resource/movie/film/actor,foaf:page,fb:people.person.gender */
   
   /****
    * A vector of URI specifying relations starting with the core_item. 
    * The final URI has range as a data type (literal, numbers)
    * The initial URI is assumed to be core_item
    *  TODO : Currently not namespace aware
    *  
    * 
    */
   /**
    * Enum specifying how to aggregate values for a 1->n relationship
    */
   public enum ValueAggregator { INDEPENDENT_VAL, COUNT, MODE, AVG, MIN, MAX }
   
   /**
    * Enum specifying what is the datatype of the target value to be predicted
    */
   public enum ValueType { NOMINAL, ENUMERATED, BINNED }
   
   private ValueType mValueType;
   private ValueAggregator mAggregatorType;
   
   /*The bins to use if value type is binned*/
   List<BinnedType> mBins;
   
   List<URI> mProperties;
   
   public RbcAttribute(List<URI> props) {
      this.mProperties = props;
   }
   
   public ValueAggregator getAggregatoraType(){
      return mAggregatorType;
   }
   public ValueType getValueType(){
      return mValueType;
   }
   
}