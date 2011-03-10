/**
 * 
 */
package airldm2.core;

/**
 * @author neeraj
 * 
 */
public class DefaultSufficentStatisticImpl implements ISufficentStatistic {

   Double value;

   public void setValue(Double value) {
      this.value = value;
   }

   public Double getValue() {
      return value;
   }

   public DefaultSufficentStatisticImpl(double val) {
      value = val;
   }

   public DefaultSufficentStatisticImpl() {

   }

}
