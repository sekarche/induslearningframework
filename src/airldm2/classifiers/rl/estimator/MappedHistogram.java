package airldm2.classifiers.rl.estimator;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;

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

   public Histogram induce(List<URI> subset) {
      double[] valueIndexCount = new double[subset.size()];
      for (int i = 0; i < valueIndexCount.length; i++) {
         String strValue = subset.get(i).toString();
         valueIndexCount[i] = get(strValue);
      }
      return new Histogram(valueIndexCount);
   }
   
}
