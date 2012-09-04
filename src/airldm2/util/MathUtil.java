package airldm2.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openrdf.model.URI;

import umontreal.iro.lecuyer.util.Num;
import airldm2.classifiers.rl.estimator.Histogram;
import airldm2.constants.Constants;

public class MathUtil {
   
   public static final double LN2 = Math.log(2.0); 
   
   public static double logMultinomialDist(int n, double[] ps, int[] x) {
      double result = Num.lnFactorial(n);
      for (int i = 0; i < ps.length; i++) {
         result += x[i] * Math.log(ps[i]);
         result -= Num.lnFactorial(x[i]);
      }
      return result;
   }
   
   public static double logBernoulliDist(double[] ps, int[] x) {
      double result = 0.0;
      for (int i = 0; i < ps.length; i++) {
         if (x[i] > 0) {
            result += Math.log(ps[i]);
         } else {
            result += Math.log(1.0 - ps[i]);
         }
      }
      return result;
   }
   
   public static void normalize(double[] a) {
      double sum = sum(a);
      if (sum <= 0.0) return;
      
      divide(a, sum);
      sum = sum(a);
      a[a.length - 1] = 1.0 - (sum - a[a.length - 1]); 
   }

   public static double averageLog(double[] logs) {
      double max = max(logs);
      double norm = 0.0;
      for (int i = 0; i < logs.length; i++) {
         norm += Math.exp(logs[i] - max);
      }
      norm = Math.log(norm) + max;
      return norm - Math.log(logs.length);
   }
   
   public static double sum(List<Double> list) {
      double sum = 0.0;
      for (double v : list) {
         sum += v;
      }
      return sum;
   }
   
   public static double average(List<Double> list) {
      return sum(list) / list.size();
   }
   
   public static double averageLog(List<Double> logList) {
      if (logList.size() == 1) return logList.get(0);
      
      double[] logs = new double[logList.size()];
      for (int i = 0; i < logs.length; i++) {
         logs[i] = logList.get(i);
      }
      return averageLog(logs);
   }
   
   public static void normalizeLog(double[] logs) {
      double max = max(logs);
      double norm = 0.0;
      for (int i = 0; i < logs.length; i++) {
         norm += Math.exp(logs[i] - max);
      }
      norm = Math.log(norm) + max;
      add(logs, -norm);
      exp(logs);
   }
   
   public static double sumLog(double[] logs) {
      double max = max(logs);
      double norm = 0.0;
      for (int i = 0; i < logs.length; i++) {
         norm += Math.exp(logs[i] - max);
      }
      norm = Math.log(norm) + max;
      return norm;
   }

   private static void exp(double[] a) {
      for (int i = 0; i < a.length; i++) {
         a[i] = Math.exp(a[i]);
      }
   }

   private static double max(double[] a) {
      double m = a[0];
      for (int i = 1; i < a.length; i++) {
         if (a[i] > m) {
            m = a[i];
         }
      }
      return m;
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
   
   public static void add(double[] target, double[] source) {
      if (target.length != source.length) {
         throw new IllegalArgumentException("Support sizes not equal: " + target.length + " != " + source.length);
      }
      
      for (int i = 0; i < target.length; i++) {
         target[i] += source[i];
      }
   }
   
   public static double sumValues(Map<URI, Double> valueHistogram) {
      double sum = 0.0;
      for (double v : valueHistogram.values()) {
         sum += v;
      }
      return sum;
   }
   
   public static <K> Map<K, Double> sumAcross(List<Map<K, Double>> histograms) {
      Map<K, Double> sumHistogram = CollectionUtil.makeMap();
      for (Map<K, Double> histogram : histograms) {
         for (Entry<K, Double> entry : histogram.entrySet()) {
            K key = entry.getKey();
            Double value = entry.getValue();
            Double sum = sumHistogram.get(key);
            if (sum == null) {
               sum = 0.0;
            }
            sum += value;
            sumHistogram.put(key, sum);
         }
      }
      
      return sumHistogram;
   }

   public static Histogram sumAcross(Histogram[] histograms) {
      final int SUPPORT = histograms[0].size();
      double[] counts = new double[SUPPORT];
      for (int k = 0; k < SUPPORT; k++) {
         for (int j = 0; j < histograms.length; j++) {
            counts[k] += histograms[j].get(k);
         }
      }
      return new Histogram(counts);
   }

   
   public static double lg(double p) {
      return Math.log(p) / LN2;
   }

   public static double getEntropy(double[] as) {
      double sum = sum(as);
      double entropySum = 0.0;
      for (double c : as) {
         if (c < Constants.EPSILON) continue;
         
         double p = c / sum;
         entropySum -= p * MathUtil.lg(p);
      }
      return entropySum;
   }

}
