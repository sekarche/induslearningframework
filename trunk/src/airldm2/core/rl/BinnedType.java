package airldm2.core.rl;

/**
 * 
 * TODO A place holder for the.
 *
 * @author neeraj (neeraj.kaul@gmail.com)
 * @since Jan 26, 2011
 * @version $Date: $
 */
public class BinnedType {
  private  Float  start;
  private  Float end;
 private   boolean START_IS_MIN;
 private   boolean END_IS_MAX;
 
    public BinnedType(float startValue, float endValue) {
    this.start = new Float(startValue);
    this.end   = new Float (endValue);
 }
     public BinnedType(float value, boolean  isStart) {
     if(isStart) {
        this.start = new Float(value);
        END_IS_MAX = true;
     } else {
        this.end = new Float(value);
        START_IS_MIN = true;
     }
     
    }  
     /**
      * 
      * @return start value
      * Null is returned if not set
      */
     public Float getStart() {
        if (START_IS_MIN) {
           return null;
        }
        else
           return start;
     }
     /**
      * 
      * @return end value
      * Null is returned if value not set
      */
     public Float getEnd() {
        if (END_IS_MAX) {
           return null;
        }
        else
           return end;
     }
     
}
