package airldm2.classifiers.rl;

import java.util.Arrays;

public class ClassCount {
   
   //[class value]
   private double[] mCount;
   
   public ClassCount(double[] c) {
      mCount = c;
   }

   public double get(int c) {
      return mCount[c];
   }
   
   public int size() {
      return mCount.length;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof ClassCount)) return false;
      ClassCount o = (ClassCount) obj;
      return Arrays.equals(mCount, o.mCount);
   }
   
   @Override
   public String toString() {
      return Arrays.toString(mCount);
   }
   
}
