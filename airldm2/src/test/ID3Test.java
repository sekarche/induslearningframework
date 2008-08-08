/**
 * ID3Test.java TODO Write description for ID3Test.java.
 * 
 * $Header: /home/CVS/airldm2/src/test/ID3Test.java,v 1.1 2008/03/31
 * 09:15:15 kansas Exp $
 */

package test;

import airldm2.classifiers.trees.Id3SimpleClassifier;
import airldm2.core.LDInstance;
import airldm2.core.LDInstances;
import airldm2.core.LDTestInstances;
import airldm2.core.datatypes.relational.ColumnDescriptor;
import airldm2.core.datatypes.relational.SingleRelationDataDescriptor;
import airldm2.util.SimpleArffFileReader;

/**
 * TODO Write class description here.
 * 
 * @author Vikas Bahirwani (TODO Write email id here)
 * @since Mar 27, 2008
 * @version $Date: 2008/08/04 16:06:30 $
 */
public class ID3Test {

   /**
    * @param args 1- to use HouseVotes data
    * @param args 2- to use WeatherNominal data
    */
   public static void main(String[] args) {
      // TODO Auto-generated method stub
      if (args.length < 1) {
         System.err.println("Incorrect input: usage <1 or 2>");
         System.exit(0);
      }
      try {
         boolean debug = false;
         String base = System.getProperty("user.dir");
         String trainFileName = "", testFileName = "";
         String class1, class2;
         if (args[0].equals("1")) {
            trainFileName = base + "/sample/HouseVotesTrain.arff";
            testFileName = base + "/sample/HouseVotesTest.arff";
            class1 = "democrat";
            class2 = "republican";
         } else {
            trainFileName = base + "/sample/WeatherNominal.arff";
            testFileName = base + "/sample/WeatherNominal.arff";
            class1 = "yes";
            class2 = "no";
         }

         // String testFileName = base + "/sample/test.arff";

         // for confusion matrix
         int dd = 0;
         int dr = 0;
         int rd = 0;
         int rr = 0;
         SimpleArffFileReader readTrain, readTest;
         LDInstances instances = null;
         LDTestInstances testInst = null;
         readTrain = new SimpleArffFileReader(trainFileName);
         readTest = new SimpleArffFileReader(testFileName);

         long startReadingTime = System.currentTimeMillis();
         instances = readTrain.getLDInstances(true);
         long endReadingTime = System.currentTimeMillis();

         testInst = readTest.getTestInstances();

         long readingTime = endReadingTime - startReadingTime;

         SingleRelationDataDescriptor desc = (SingleRelationDataDescriptor) instances
               .getDesc();
         if (debug) {
            printTestInstances(testInst);
         }
         Id3SimpleClassifier id3 = new Id3SimpleClassifier();
         // Id3Classifier id3 = new Id3Classifier();
         long startBuildingTime = System.currentTimeMillis();
         id3.buildClassifier(instances);
         long endBuildingTime = System.currentTimeMillis();
         long buildingTime = endBuildingTime - startBuildingTime;

         System.out.println(id3.toString());
         for (int i = 0; i < testInst.getNumberInstances(); i++) {
            LDInstance currInstance = testInst.getLDInstance(i);
            double index = id3.classifyInstance(currInstance);

            int resIndex = new Double(index).intValue();

            ColumnDescriptor labelAttribute = desc.getTableDesc().getColumns()
                  .lastElement();

            String predictedClass = labelAttribute.getPossibleValues().get(
                  resIndex);
            String actualClass = currInstance.getClassLabel();
            /*
             * System.out.println("predicted class=" + predictedClass + "
             * Actual Class=" + actualClass);
             */
            if (actualClass.trim().equals(class1)) {
               if (predictedClass.equals(class1)) {
                  dd += 1;
               } else {
                  dr += 1;
               }

            } else if (actualClass.trim().equals(class2)) {
               if (predictedClass.equals(class1)) {
                  rd += 1;
               } else {
                  rr += 1;
               }
            }

         }
         System.out.println("CONFUSION MATRIX");
         System.out.println();

         System.out.println("\t democrat \t republican");
         System.out.println("\t -------------------------");
         System.out.println("democrat \t " + dd + "\t" + dr);
         System.out.println("republican \t " + rd + "\t" + rr);
         System.out.println("Reading time= " + readingTime);
         System.out.println("Classifier building time= " + buildingTime);

      } catch (Exception e) {
         e.printStackTrace();
         System.out.println("Weirdo");
      }

   }

   private static void printTestInstances(LDTestInstances testInst)
         throws Exception {
      for (int i = 0; i < testInst.getNumberInstances(); i++) {
         LDInstance currInstance = testInst.getLDInstance(i);
         System.out.println("(" + i + "):"
               + currInstance.getStringRepresentation());

      }
   }
}
