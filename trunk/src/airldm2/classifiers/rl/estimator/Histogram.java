package airldm2.classifiers.rl.estimator;
import java.util.Arrays;

import airldm2.constants.Constants;
import airldm2.util.MathUtil;

public class Histogram implements AttributeValue {
   
   //[value index]
   private double[] mCount;
   
   public Histogram(double[] c) {
      mCount = c;
   }

   public Histogram(int size) {
      mCount = new double[size];
   }

   public double get(int i) {
      return mCount[i];
   }
   
   public int[] getIntArray(int correction) {
      int[] array = new int[mCount.length];
      for (int i = 0; i < mCount.length; i++) {
         array[i] = (int) mCount[i] + correction;
      }
      return array;
   }
   
   public int size() {
      return mCount.length;
   }
   
   public double sum() {
      return MathUtil.sum(mCount);
   }

   public boolean containsZeroCount() {
      for (int i = 0; i < mCount.length; i++) {
         if (mCount[i] < Constants.EPSILON) return true;
      }
      return false;
   }

   public double getEntropy() {
      return MathUtil.getEntropy(mCount);
   }

   public Histogram copy() {
      return new Histogram(mCount.clone());
   }
   
   public void add(Histogram other) {
      MathUtil.add(mCount, other.mCount);
   }
   
   public void add(double[] cs) {
      MathUtil.add(mCount, cs);
   }
   
   public static Histogram make1ofK(int index, int K) {
      double[] count = new double[K];
      count[index] = 1;
      return new Histogram(count);
   }
   
   public static Histogram[] makeArray(double[][] v) {
      Histogram[] array = new Histogram[v.length];
      for (int i = 0; i < v.length; i++) {
         array[i] = new Histogram(v[i]);
      }
      return array;
   }
   
   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof Histogram)) return false;
      Histogram o = (Histogram) obj;
      return Arrays.equals(mCount, o.mCount);
   }
   
   @Override
   public String toString() {
      return Arrays.toString(mCount);
   }

}
