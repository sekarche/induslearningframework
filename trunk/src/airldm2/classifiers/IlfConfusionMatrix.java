/**
 * ConfusionMatrix.java<br>
 * TODO Write description for ConfusionMatrix.java.
 *
 * $Header: $
 */

package airldm2.classifiers;

import airldm2.core.DataDescriptor;
import airldm2.core.datatypes.relational.SingleRelationDataDescriptor;

import java.util.Vector;
/**
 * TODO Write class description here.
 *
 * @author neeraj (TODO Write email id here)
 * @since Aug 10, 2008
 * @version $Date: $
 */
public class IlfConfusionMatrix{
      double[][] matrix;
      Vector<String> possibleClassLabels;
   
      IlfConfusionMatrix(DataDescriptor desc,  double[] actual, double[] predictions) {
      
      //The actual and predictions can be easily used to construct a fast vector of Nominal Predictions
      //in Weka
      SingleRelationDataDescriptor tableDesc = (SingleRelationDataDescriptor) desc;
      int numClassLabels = tableDesc.getClassLabelDescriptor().getNumValues();
      this.possibleClassLabels = tableDesc.getClassLabelDescriptor().getPossibleValues();
      matrix = new double[numClassLabels][numClassLabels];
      buildMatrix(actual,predictions);
      
   }
   
   
   private  void buildMatrix( double[] actual, double[] predictions) {
      //TODO: verify actual is not null
      int row;
      int col;
      for (int i=0; i < actual.length; i ++) {
         row = (int) actual[i];
         col = (int) predictions[i];
         matrix[row][col]++;
      }
   }
   
   public void printMatrix() {
   
      String spaceForActual="      "; //space taken by keyword actual
      System.out.println(spaceForActual +  "\t\t\t\t\t  Predicted -->");
       System.out.print(spaceForActual + "\t\t\t\t");
       for (int i =0; i < possibleClassLabels.size(); i++) {
       System.out.print(possibleClassLabels.get(i) + "\t");
       }
       System.out.println("");
       System.out.println("actual");
      int size = possibleClassLabels.size();
       
       for (int i =0; i < possibleClassLabels.size(); i++) {
          System.out.print(spaceForActual + "\t" + possibleClassLabels.get(i) + "\t");
          for (int j =0; j < possibleClassLabels.size(); j ++ ) {
             System.out.print(matrix[i][j] + "\t");
          }
          System.out.println("");
          }
       }
   
       
   }
   
   


