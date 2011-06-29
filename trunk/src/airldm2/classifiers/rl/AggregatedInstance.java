package airldm2.classifiers.rl;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class AggregatedInstance {

   /* mFeatureValueIndexOrCount[attribute index][value index] = count of value occurrences
    * If mAggregatorType != INDEPENDENT_VAL, then the sum of counts is exactly 1
    */
   private int[][] mFeatureValueIndexCount;
   
   private int[] mTargetValueIndexCount;

   public AggregatedInstance(int[][] featureValueIndexCount, int[] targetValueIndexCount) {
      mFeatureValueIndexCount = featureValueIndexCount;
      mTargetValueIndexCount = targetValueIndexCount;
   }

   public int[][] getFeatureValueIndexCount() {
      return mFeatureValueIndexCount;
   }

   public int getLabel() {
      for (int i = 0; i < mTargetValueIndexCount.length; i++) {
         if (mTargetValueIndexCount[i] > 0) {
            return i;
         }
      }
      
      return -1;
   }
   
   @Override
   public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
   }
   
}
