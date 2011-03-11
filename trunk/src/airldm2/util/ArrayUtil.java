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
   
}
