package airldm2.core.rl;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;

import airldm2.util.ArrayUtil;
import airldm2.util.CollectionUtil;
import airldm2.util.StringUtil;

public class BinnedType implements DiscreteType {
   
   public static String NAME = "BINNED";
   
   /**
    * Example:
    *  Cut Point = [ 3, 5, 10 ]
    *  Bin index:
    *    (-infty, 3) = 0
    *    [3, 5) = 1
    *    [5, 10) = 2
    *    [10, +infty) = 3
    */
   private double[] mCutPoints;

   public BinnedType(double[] cutPoints) {
      mCutPoints = cutPoints;      
   }

   public int getBinIndex(double v) {
      for (int i = 0; i < mCutPoints.length; i++) {
         if (v < mCutPoints[i]) return i;
      }
      return mCutPoints.length;
   }
   
   @Override
   public int domainSize() {
      return mCutPoints.length + 1;
   }

   @Override
   public int indexOf(Value value) {
      if (value instanceof Literal) {
         Literal lit = (Literal) value;
         double doubleValue = lit.doubleValue();
         return ArrayUtil.indexOf(mCutPoints, doubleValue);
      }
      
      return -1;
   }

   @Override
   public String makeFilter(String varName, int valueIndex) {
      StringBuilder b = new StringBuilder();
      b.append("FILTER(");
      
      if (valueIndex == 0) {
         b.append(varName).append(" < ").append(mCutPoints[valueIndex]);
      } else if (valueIndex == mCutPoints.length) {
         b.append(varName).append(" >= ").append(mCutPoints[valueIndex - 1]);
      } else {
         b.append(varName).append(" >= ").append(mCutPoints[valueIndex - 1]);
         b.append(" && ");
         b.append(varName).append(" < ").append(mCutPoints[valueIndex]);
      }
      b.append(") ");
      
      return b.toString();
   }

   public String toString() {
      return NAME + "=" + StringUtil.toCSV(mCutPoints);
   }

   @Override
   public List<String> getStringValues() {
      List<String> strings = CollectionUtil.makeList();
      for (int i = 0; i < mCutPoints.length; i++) {
         strings.add(String.valueOf(mCutPoints[i]));
      }
      strings.add("INF");
      return strings;
   }

   @Override
   public void write(Writer out) throws IOException {
      out.write(NAME);
      out.write("=");
      out.write(StringUtil.toCSV(mCutPoints));
   }

}