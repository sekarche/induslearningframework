package airldm2.classifiers.rl.estimator;
import java.util.Map;

public class MappedHistogram extends Histogram {
   
   private Map<String, Integer> mMappedCounts;

   public MappedHistogram(double[] c, Map<String, Integer> mapped) {
      super(c);
      mMappedCounts = mapped;
   }

   public int get(String key) {
      Integer result = mMappedCounts.get(key);
      if (result == null) return 0;
      return result;
   }
   
}
