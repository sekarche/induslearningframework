package airldm2.core.rl;

import java.io.IOException;
import java.io.Writer;

public class NumericType implements ValueType {
   
   public enum Distribution { POISSON, EXPONENTIAL, GAUSSIAN }
   
   public static String NAME = "NUMERIC";
   
   private Distribution mDist;

   public NumericType(Distribution dist) {
      mDist = dist;
   }

   public Distribution getDist() {
      return mDist;
   }

   public String toString() {
      return NAME + "=" + mDist;
   }

   @Override
   public void write(Writer out) throws IOException {
      out.write(NAME);
      out.write("=");
      out.write(mDist.toString());
   }

}