package airldm2.classifiers.rl;

import java.util.Arrays;

public class ClassValueCount {
   
   //[class value][attribute value]
   private double[][] mCount;
   
   public ClassValueCount(double[][] c) {
      mCount = c;
   }

   public double get(int c, int v) {
      return mCount[c][v];
   }

   public int size(int i) {
      return mCount[i].length;
   }
   
   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof ClassValueCount)) return false;
      ClassValueCount o = (ClassValueCount) obj;
      return Arrays.deepEquals(mCount, o.mCount);
   }
   
   @Override
   public String toString() {
      return Arrays.deepToString(mCount);
   }
   
}
