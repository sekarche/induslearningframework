package airldm2.classifiers.rl.estimator;

import java.util.Arrays;

public class Histogram implements AttributeValue {
   
   //[value index]
   private double[] mCount;
   
   public Histogram(double[] c) {
      mCount = c;
   }

   public double get(int i) {
      return mCount[i];
   }
   
   public int size() {
      return mCount.length;
   }
   
   public double sum() {
      double sum = 0;
      for (int i = 0; i < mCount.length; i++) {
         sum += mCount[i];
      }
      return sum;
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
