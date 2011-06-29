package explore;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import airldm2.core.rl.RbcAttribute;

public class RbcAttributeScore implements Comparable<RbcAttributeScore> {
   
   public RbcAttribute Attribute;
   public double Score;
   
   public RbcAttributeScore(RbcAttribute a, double score) {
      Attribute = a;
      Score = score;
   }

   @Override
   public int compareTo(RbcAttributeScore o) {
      double diff = Score - o.Score;
      if (Math.abs(diff) < 0.0000001) return 0;
      if (diff < 0.0) return -1;
      return 1;
   }
   
   @Override
   public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
   }
}