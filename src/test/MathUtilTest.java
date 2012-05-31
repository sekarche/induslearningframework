package test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import airldm2.util.MathUtil;

public class MathUtilTest {

   @Test
   public void testIndexOf1() {
      double[] cutpoints = new double[] {3};
      assertEquals(0, MathUtil.indexOf(cutpoints, 1));
      assertEquals(1, MathUtil.indexOf(cutpoints, 3));
      assertEquals(1, MathUtil.indexOf(cutpoints, 5));
   }
   
   @Test
   public void testIndexOf2() {
      double[] cutpoints = new double[] {3, 5, 10};
      assertEquals(0, MathUtil.indexOf(cutpoints, 1));
      assertEquals(1, MathUtil.indexOf(cutpoints, 3));
      assertEquals(2, MathUtil.indexOf(cutpoints, 5));
      assertEquals(3, MathUtil.indexOf(cutpoints, 10));
      assertEquals(3, MathUtil.indexOf(cutpoints, 11));
   }
   
}
