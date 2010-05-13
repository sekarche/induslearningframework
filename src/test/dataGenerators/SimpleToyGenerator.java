/**
 * SimpleToyGenerator.java<br>
 * TODO Write description for SimpleToyGenerator.java.
 * 
 * $Header: /home/CVS/airldm2/src/test/dataGenerators/SimpleToyGenerator.java,v 1.1 2008/04/02 18:07:14 kansas Exp $
 */

package test.dataGenerators;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Random;

/**
 * TODO Write class description here.
 * 
 * @author Vikas Bahirwani (TODO Write email id here)
 * @since Apr 2, 2008
 * @version $Date: 2008/04/02 18:07:14 $
 */
public class SimpleToyGenerator {

   int possValues[] = null;

   String fileNamePrefix;

   int numOfInstances[] = null;

   static boolean DEBUG = true;

   /**
    * @param args
    */
   public static void main(String[] args) {
      try {
         String base = System.getProperty("user.dir");
         String path = base + "/sample/autoGenerated/";
         String fileName = path + "metaData.txt";
         BufferedReader metaData = new BufferedReader(new FileReader(fileName));
         SimpleToyGenerator stg = new SimpleToyGenerator();
         stg.extractMetaInformation(metaData);
         metaData.close();
         stg.generateData(path);
      } catch (Exception e) {
         e.printStackTrace();
      }
      // TODO Auto-generated method stub

   }

   public void extractMetaInformation(BufferedReader _input) throws Exception {
      if (_input == null) {
         Debug("Inside extractMetaInformationString(): _input=null");
         throw new Exception("BufferedReader _input=null");
      } else {
         int numOfAttribs = -1, numOfFiles = -1;
         String s = _input.readLine();
         int index = -1;
         while (s != null) {
            if (s.equals("") || s.charAt(0) == '%') {
               // Debug("Inside getInformation(): Comment or blank
               // encountered");
            } else {
               if (s.length() >= 17) {
                  if (s.substring(0, 17).equalsIgnoreCase("@numOfAttributes=")) {
                     numOfAttribs = Integer.parseInt(s.substring(17));
                     Debug("Inside getInformation(): numOfAttribs="
                           + numOfAttribs);
                     if (possValues != null) {
                        throw new Exception(
                              "Number of attributes specified more than once");
                     } else {
                        possValues = new int[numOfAttribs];
                     }
                  }
               }
               if (s.equalsIgnoreCase("@attributes")) {
                  if (numOfAttribs <= 0 || possValues == null) {
                     throw new Exception("Corrupt input file");
                  } else {
                     for (int i = 0; i < numOfAttribs; i++) {
                        s = _input.readLine();
                        if (s == null)
                           throw new Exception("Corrupt input file");
                        if (s.substring(0, 6).equalsIgnoreCase("attrib")
                              || s.substring(0, 5).equalsIgnoreCase("class")) {
                           index = s.indexOf(' ');
                           possValues[i] = Integer.parseInt(s
                                 .substring(index + 1));
                           Debug("Inside getInformation(): attrib" + i
                                 + " number of possible values="
                                 + possValues[i]);
                        } else {
                           throw new Exception("Corrupt input file");
                        }
                     }
                  }
               }
               if (s.length() >= 12) {
                  if (s.substring(0, 12).equalsIgnoreCase("@numOfFiles=")) {
                     numOfFiles = Integer.parseInt(s.substring(12));
                     Debug("Inside getInformation(): numOfFiles=" + numOfFiles);
                     if (numOfInstances != null) {
                        throw new Exception(
                              "Number of files specified more than once");
                     } else {
                        numOfInstances = new int[numOfFiles];
                     }
                  }
               }
               if (s.length() >= 16) {
                  if (s.substring(0, 16).equalsIgnoreCase("@fileNamePrefix=")) {
                     fileNamePrefix = s.substring(16);
                     Debug("Inside getInformation(): fileNamePrefix="
                           + fileNamePrefix);
                  }
               }
               if (s.equalsIgnoreCase("@files")) {
                  for (int i = 0; i < numOfFiles; i++) {
                     s = _input.readLine();
                     if (s == null)
                        throw new Exception("Corrupt input file");
                     if (s.substring(0, 4).equals("file")) {
                        index = s.indexOf(' ');
                        numOfInstances[i] = Integer.parseInt(s
                              .substring(index + 1));
                        Debug("Inside getInformation(): file" + i
                              + " number of instances=" + numOfInstances[i]);

                     } else {
                        throw new Exception("Corrupt input file");
                     }
                  }
               }
            }
            s = _input.readLine();
         }
      }
   }

   public void generateData(String path) throws Exception {
      Random randomNumber;
      String relationName, temp = "";
      PrintWriter _pw;
      String filename;
      for (int i = 0; i < numOfInstances.length; i++) {
         relationName = fileNamePrefix;
         randomNumber = new Random(System.currentTimeMillis());
         relationName=relationName.concat(Integer.toString(numOfInstances[i]));
         _pw = new PrintWriter(new FileWriter(path + relationName + ".arff"));

         // Writing into the arff file
         _pw.println("@relation " + relationName);

         // Declaring attribute list
         _pw.println();
         for (int j = 0; j < (possValues.length - 1); j++) {
            temp = "@attribute attrib" + Integer.toString(j) + " {";
            int k;
            for (k = 0; k < possValues[j] - 1; k++) {
               temp=temp.concat(Integer.toString(k) + ',');
            }
            temp=temp.concat(Integer.toString(k) + '}');
            _pw.println(temp);
         }

         temp = "@attribute class {";
         int k;
         for (k = 0; k < possValues[possValues.length - 1] - 1; k++) {
            temp=temp.concat(Integer.toString(k) + ',');
         }
         temp=temp.concat(Integer.toString(k) + '}');
         _pw.println(temp);

         // writing the data part
         _pw.println("\n@data");
         for (int j = 0; j < numOfInstances[i]; j++) {
            temp = "";
            k = 0;
            for (k = 0; k < possValues.length - 1; k++) {
               temp=temp.concat(Integer
                     .toString(randomNumber.nextInt(possValues[k])) + ',');
            }
            temp=temp.concat(Integer.toString(randomNumber.nextInt(possValues[k])));
            _pw.println(temp);
         }
         _pw.close();
         Debug("Inside generateData(): "+relationName+ ".arff written");
      }
   }

   public void Debug(Object o) {
      if (DEBUG)
         System.out.println(o);
   }

}