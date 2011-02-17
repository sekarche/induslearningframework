package airldm2.core.rl;

import java.util.Arrays;

public class BinnedType {
   
   /**
    * Example:
    *  Cut Point = [ 3, 5, 10 ]
    *  Bin index:
    *    (-infty, 3) = 0
    *    [3, 5) = 1
    *    [5, 10) = 2
    *    [10, +infty) = 3
    */
   private double[] mCutPoints;

   public BinnedType(double[] cutPoints) {
      mCutPoints = cutPoints;      
   }
   
   public int size() {
      return mCutPoints.length + 1;
   }

   public int getBinIndex(double v) {
      for (int i = 0; i < mCutPoints.length; i++) {
         if (v < mCutPoints[i]) return i;
      }
      return mCutPoints.length;
   }
   
   public double[] getCutPoints() {
      return mCutPoints;
   }
   
   @Override
   public String toString() {
      return Arrays.toString(mCutPoints);
   }
}
