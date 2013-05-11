package airldm2.classifiers.rl.ontology;

import java.util.Arrays;
import java.util.List;

import airldm2.util.CollectionUtil;

public class CutProfile {

   private List<Cut> mCut;
   private List<Double> mValue;
   
   public CutProfile() {
      mCut = CollectionUtil.makeList();
      mValue = CollectionUtil.makeList();
   }
   
   public Cut getCutWithSize(int cutSize) {
      for (Cut c : mCut) {
         if (c.size() >= cutSize) {
            return c;
         }
      }
      
      return null;
   }

   public void add(Cut cut, double mi) {
      mCut.add(cut);
      mValue.add(mi);
   }

   private double[][] getArray() {
      double[][] result = new double[mCut.size()][2];
      
      for (int i = 0; i < mCut.size(); i++) {
         result[i][0] = mCut.get(i).size();
         result[i][1] = mValue.get(i);
      }
      
      System.out.println(Arrays.deepToString(result));
      return result;
   }
   
   public double[][] interpolate() {
      double[][] array = getArray();
      
      List<double[]> interpolated = CollectionUtil.makeList();
      double[] lastValues = array[0];
      interpolated.add(array[0]);
      
      for (int i = 1; i < array.length; i++) {
         int diff = (int) (array[i][0] - lastValues[0]);
         for (int c = 1; c <= diff; c++) {
            double[] newValues = new double[array[i].length];
            newValues[0] = lastValues[0] + c;
            for (int v = 1; v < array[i].length; v++) {
               double slope = (array[i][v] - lastValues[v]) / diff;
               newValues[v] = lastValues[v] + slope * c;
            }
            interpolated.add(newValues);
         }
         
         lastValues = array[i];
      }
      
      double[][] result = new double[interpolated.size()][];
      for (int i = 0; i < result.length; i++) {
         result[i] = interpolated.get(i);
      }
      
      System.out.println(Arrays.deepToString(result));
      return result;
   }

}
