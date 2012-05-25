package airldm2.classifiers.rl.estimator;

public class Category implements AttributeValue {

   private int mIndex;
   
   public Category(int index) {
      mIndex = index;
   }

   public int getIndex() {
      return mIndex;
   }

   @Override
   public String toString() {
      return String.valueOf(mIndex);
   }
   
}
