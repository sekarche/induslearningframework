package airldm2.classifiers.rl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import airldm2.util.CollectionUtil;

public class WeightedRandom {

   private Map<String, Double> mWeight;
   private Random mRandom;
   
   private List<String> mWeightList;
   private double[] mAccWeight;
   private double mSum;
   
   public WeightedRandom(Map<String, Double> weight, Random random) {
      mWeight = weight;
      mRandom = random;
      
      mWeightList = CollectionUtil.makeList(mWeight.keySet());
      mAccWeight = new double[mWeightList.size()];
      for (int i = 0; i < mAccWeight.length; i++) {
         double last = (i == 0) ? 0.0 : mAccWeight[i - 1];
         
         mAccWeight[i] = last + mWeight.get(mWeightList.get(i));
      }
      
      mSum = mAccWeight[mAccWeight.length - 1];
      
      
      double[] test = new double[mAccWeight.length];
      for (int i = 0; i < test.length; i++) {
         test[i] = mWeight.get(mWeightList.get(i));
      }
      Arrays.sort(test);
      System.out.println(test[0] + " " + test[test.length / 4] + " " + test[test.length / 2] + " " + test[test.length * 3 / 4] + " " + test[test.length - 1]);
   }

   public String next() {
      double target = mRandom.nextDouble() * mSum;
      for (int i = 0; i < mAccWeight.length; i++) {
         double last = (i == 0) ? 0.0 : mAccWeight[i - 1];
         
         if (target >= last && target < mAccWeight[i]) {
            return mWeightList.get(i);
         }
      }
      
      return null;
   }

}
