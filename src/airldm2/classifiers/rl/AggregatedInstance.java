package airldm2.classifiers.rl;

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

   public int[] getTargetValueIndexCount() {
      return mTargetValueIndexCount;
   }
   
   
}
