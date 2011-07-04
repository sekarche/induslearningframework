package airldm2.classifiers.rl;

import java.util.Arrays;

public class ValueIndexCount {
   
   //[value index]
   private int[] mCount;
   
   public ValueIndexCount(int[] c) {
      mCount = c;
   }

   public int get(int c) {
      return mCount[c];
   }
   
   public int size() {
      return mCount.length;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof ValueIndexCount)) return false;
      ValueIndexCount o = (ValueIndexCount) obj;
      return Arrays.equals(mCount, o.mCount);
   }
   
   @Override
   public String toString() {
      return Arrays.toString(mCount);
   }
   
}
