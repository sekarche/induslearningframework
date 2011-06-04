package airldm2.util;

public class ArrayUtil {

   public static void normalize(double[] a) {
      double sum = 0.0;
      for (int i = 0; i < a.length; i++) {
         sum += a[i];
      }
      if (sum <= 0.0) return;
      
      for (int i = 0; i < a.length; i++) {
         a[i] /= sum;
      }
   }

   public static int maxIndex(int[] a) {
      int currentMax = a[0];
      int currentMaxIndex = 0;
      for (int i = 1; i < a.length; i++) {
         if (a[i] > currentMax) {
            currentMax = a[i];
            currentMaxIndex = i;
         }
      }
      return currentMaxIndex;
   }
   
   public static int maxIndex(double[] a) {
      double currentMax = a[0];
      int currentMaxIndex = 0;
      for (int i = 1; i < a.length; i++) {
         if (a[i] > currentMax) {
            currentMax = a[i];
            currentMaxIndex = i;
         }
      }
      return currentMaxIndex;
   }

   /**
    * Example:
    *  Cut Point = [ 3, 5, 10 ]
    *  indexOf:
    *    (-infty, 3) = 0
    *    [3, 5) = 1
    *    [5, 10) = 2
    *    [10, +infty) = 3
    */
   public static int indexOf(double[] cutPoints, double v) {
      if (v < cutPoints[0]) return 0;
      
      int i = 1;
      for (; i < cutPoints.length && cutPoints[i] <= v; i++);
      return i;
   }
   
   public static void add(double[][][] a, double shift) {
      for (int i = 0; i < a.length; i++) {
         for (int j = 0; j < a[i].length; j++) {
            add(a[i][j], shift);
         }
      }
   }
   
   public static void add(double[] a, double shift) {
      for (int i = 0; i < a.length; i++) {
         a[i] += shift;
      }
   }

   public static int[] castToInt(double[] aDouble) {
      int[] aInt = new int[aDouble.length];
      for (int i = 0; i < aInt.length; i++) {
         aInt[i] = (int) aDouble[i];
      }
      return aInt;
   }
   
}
