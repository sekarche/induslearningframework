package airldm2.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import airldm2.classifiers.rl.estimator.Histogram;

public class MathUtil {
   
   public static double lg(double v) {
      return Math.log(v) / Math.log(2.0);
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
   
}
