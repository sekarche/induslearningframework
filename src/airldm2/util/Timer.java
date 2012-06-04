package airldm2.util;

import java.util.Map;

public class Timer {
   
   private Map<String,Long> mStartTime = CollectionUtil.makeMap();
   private Map<String,Long> mTotal = CollectionUtil.makeMap();
   
   public void start(String name) {
      mStartTime.put(name, System.currentTimeMillis());
   }
   
   public void stop(String name) {
      long start = mStartTime.get(name);
      long duration = (System.currentTimeMillis() - start);
      Long total = mTotal.get(name);
      if (total == null) {
         total = 0L;
      }
      mTotal.put(name, total + duration);
   }
   
   @Override
   public String toString() {
      return mTotal.toString();
   }
   
}