package airldm2.classifiers.rl.estimator;

public class Numeric implements AttributeValue {

   private double mValue;
   
   public Numeric(double v) {
      mValue = v;
   }

   public double getValue() {
      return mValue;
   }
   
   @Override
   public String toString() {
      return Double.toString(mValue);
   }
   
}
