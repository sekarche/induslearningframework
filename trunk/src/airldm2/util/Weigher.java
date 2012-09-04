package airldm2.util;

import java.util.List;

import org.openrdf.model.Value;


public class Weigher {
   
   public static final Weigher INSTANCE = new Weigher();
   
   private long mTotal;
   private boolean mIsRecording = true;
   
   public void add(String s) {
      if (!mIsRecording) return;
      
      mTotal += s.length();
   }
   
   public void add(List<Value[]> rows) {
      if (!mIsRecording) return;
      
      for (Value[] row : rows) {
         for (Value v : row) {
            mTotal += v.toString().length();
         }
      }
   }
   
   public void reset() {
      mTotal = 0L;
   }
   
   @Override
   public String toString() {
      return "" + mTotal;
   }

   public void pause() {
      mIsRecording = false;
   }

   public void resume() {
      mIsRecording = true;
   }
   
}