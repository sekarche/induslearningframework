package airldm2.util;

public class ArrayUtil {

   public static void normalize(double[] a) {
      double sum = sum(a);
      if (sum <= 0.0) return;
      
      divide(a, sum);
   }

   public static double[][] normalize(double[][] a) {
      double sum = sum(a);
      if (sum <= 0.0) return null;
      
      double[][] result = copy(a);
      divide(result, sum);
      return result;
   }
   
   public static void divide(double[][] a, double v) {
      for (int i = 0; i < a.length; i++) {
         divide(a[i], v);
      }
   }

   public static void divide(double[] a, double v) {
      for (int i = 0; i < a.length; i++) {
         a[i] /= v;
      }
   }

   private static double[][] copy(double[][] a) {
      double[][] result = new double[a.length][];
      for (int i = 0; i < result.length; i++) {
         result[i] = copy(a[i]);
      }
      return result;
   }

   private static double[] copy(double[] a) {
      double[] result = new double[a.length];
      System.arraycopy(a, 0, result, 0, a.length);
      return result;
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
   
   public static void add(double[][][] a, double v) {
      for (int i = 0; i < a.length; i++) {
         add(a[i], v);
      }
   }
   
   public static void add(double[][] a, double v) {
      for (int i = 0; i < a.length; i++) {
         add(a[i], v);
      }
   }
   
   public static void add(double[] a, double v) {
      for (int i = 0; i < a.length; i++) {
         a[i] += v;
      }
   }

   public static int[] castToInt(double[] aDouble) {
      int[] aInt = new int[aDouble.length];
      for (int i = 0; i < aInt.length; i++) {
         aInt[i] = (int) aDouble[i];
      }
      return aInt;
   }
   
   public static double[] sumDimension(double[][] matrix, int dim) {
      if (dim == 1) {
         double[] sum = new double[matrix[0].length];
         for (int s = 0; s < sum.length; s++) {
            for (int i = 0; i < matrix.length; i++) {
               sum[s] += matrix[i][s];
            }
         }
         return sum;
      } else if (dim == 2) {
         double[] sum = new double[matrix.length];
         for (int s = 0; s < sum.length; s++) {
            for (int i = 0; i < matrix[0].length; i++) {
               sum[s] += matrix[s][i];
            }
         }
         return sum;
      } else return null;
   }

   public static double sum(double[][] a) {
      double sum = 0.0;
      for (int i = 0; i < a.length; i++) {
         sum += sum(a[i]);
      }
      return sum;
   }

   public static double sum(double[] a) {
      double sum = 0.0;
      for (int i = 0; i < a.length; i++) {
         sum += a[i];
      }
      return sum;
   }
   
}
