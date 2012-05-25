package airldm2.classifiers.rl.estimator;
import java.util.Arrays;

import airldm2.util.MathUtil;

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
      return MathUtil.sum(mCount);
   }

   public Histogram copy() {
      return new Histogram(mCount.clone());
   }
   
   public void add(Histogram other) {
      MathUtil.add(mCount, other.mCount);
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
