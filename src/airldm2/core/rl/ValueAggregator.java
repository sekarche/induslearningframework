package airldm2.core.rl;

/**
 * Enum specifying how to aggregate values for a 1->n relationship
 */
public enum ValueAggregator {
   NONE, HISTOGRAM, COUNT, SUM, AVG, MIN, MAX;
   
   public static boolean isNumericInput(ValueAggregator v) {
      return v == SUM
         || v == AVG
         || v == MIN
         || v == MAX;
   }
   
   public static boolean isNumericOutput(ValueAggregator v) {
      return isNumericInput(v) || v == COUNT;
   }
   
}