package airldm2.util;


public class Weigher {
   
   public static final Weigher INSTANCE = new Weigher();
   
   private long mTotal;
   
   public void add(String s) {
      mTotal += s.length();
   }
   
   @Override
   public String toString() {
      return "" + mTotal;
   }
   
}