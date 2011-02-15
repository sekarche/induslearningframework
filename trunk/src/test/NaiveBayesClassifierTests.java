/**
 * NBClassifierTest.java<br>
 * TODO Write description for NBClassifierTest.java.
 * 
 * $Header: $
 */

package test;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import weka.classifiers.evaluation.ConfusionMatrix;
import weka.core.Utils;
import airldm2.classifiers.Evaluation;
import airldm2.classifiers.bayes.NaiveBayesClassifier;
import airldm2.core.LDInstances;
import airldm2.core.LDTestInstances;
import airldm2.core.SSDataSource;
import airldm2.core.datatypes.relational.RelationalDataSource;
import airldm2.core.datatypes.relational.SingleRelationDataDescriptor;
import airldm2.util.SimpleArffFileReader;

/**
 * TODO Write class description here.
 * 
 * @author neeraj (TODO Write email id here)
 * @since Aug 12, 2008
 * @version $Date: $
 */
public class NaiveBayesClassifierTests {
   /**
    * @throws java.lang.Exception
    */
   @Before
   public void setUp() throws Exception {
   }

   @Test
   public void testNBWithOptionsArff() {

      /**
       * test for Naive Bayes to check if options such as for missing
       * values are handled
       */
      String[] options = { "-a", "-?", "-trainFile",
            "sample/HouseVotesTrain.arff", "-testFile",
            "sample/HouseVotesTrain.arff" };
      try {

         String trainFile = Utils.getOption("trainFile", options);
         String testFile = Utils.getOption("testFile", options);
         this.testNBWithTrainInArff(trainFile, testFile, options);
      } catch (Exception e) {
         Assert.fail();
         e.printStackTrace();
      }

   }

   @Test
   public void testNBMultipleValuesAttributesWithOptionsArff() {
      /**
       * Test for Naive Bayes when an attribute when takes more than one
       * possible values
       */
      String[] options = { "-a", "-?", "-trainFile",
            "sample/weather_nominal.arff", "-testFile",
            "sample/weather_nominalTest.arff" };
      try {

         String trainFile = Utils.getOption("trainFile", options);
         String testFile = Utils.getOption("testFile", options);
         this.testNBWithTrainInArff(trainFile, testFile, options);
      } catch (Exception e) {
         Assert.fail();
         e.printStackTrace();
      }

   }

   @Test
   public void testNBWithOptionsDB() {

      String[] options = { "-b", "-trainTable", "votes_train", "-testFile",
            "sample/HouseVotesTrain.arff" };
      try {

         String trainTableName = Utils.getOption("trainTable", options);
         String testFile = Utils.getOption("testFile", options);
         this.testNBWithTrainInDB2(testFile, trainTableName, options);
      } catch (Exception e) {
         Assert.fail();
         e.printStackTrace();
      }

   }

   @Test
   public void testNBEvaluationOptions() {
      try {
         NaiveBayesClassifier classifier = new NaiveBayesClassifier();
         String[] options = { "-b", "-trainTable", "votes_train", "-testFile",
               "sample/HouseVotesTrain.arff" };

         String res = Evaluation.evaluateModel(classifier, options);
         System.out.println(res);
      } catch (Exception e) {
         Assert.fail();
         e.printStackTrace();
      }
   }

   private void testNBWithTrainInArff(String trainFile, String testFile,
         String[] options) {
      /**
       * test Niave Bayes when the training file is read from arff
       */
      NaiveBayesClassifier classifier = new NaiveBayesClassifier();
      try {
         SimpleArffFileReader readTrain = new SimpleArffFileReader(trainFile);
         SimpleArffFileReader readTest = new SimpleArffFileReader(testFile);
         LDInstances trainData = readTrain.getLDInstances(true);
         LDTestInstances testData = readTest.getTestInstances();

         ConfusionMatrix matrix = Evaluation.evlauateModel(classifier,
               trainData, testData, options);
         System.out.println(matrix.toString("===Confusion Matrix==="));

         /*
          * IlfConfusionMatrix matrix =
          * Evaluation.evlauateModel(classifier, trainData, testData,
          * options); matrix.toString();
          */

      } catch (Exception e) {

         Assert.fail("Error reading instances or buildDing classifier:"
               + e.getMessage());
         e.printStackTrace();
      }

   }

   private void testNBWithTrainInDB2(String testFile, String trainTableName,
         String[] options) {
      /**
       * Naive Bayes test when the training set is in a database
       */

      NaiveBayesClassifier classifier = new NaiveBayesClassifier();

      SingleRelationDataDescriptor desc = null;

      try {
         SimpleArffFileReader readTest = new SimpleArffFileReader(testFile);
         LDTestInstances testInst = readTest.getTestInstances();
         desc = (SingleRelationDataDescriptor) testInst.getDesc();

         SSDataSource dataSource = new RelationalDataSource(trainTableName);
         // Create a Large DataSet Instance and set its
         // descriptor and source
         LDInstances trainData = new LDInstances();
         trainData.setDesc(desc);
         trainData.setDataSource(dataSource);

         ConfusionMatrix matrix = Evaluation.evlauateModel(classifier,
               trainData, testInst, options);
         System.out.println(matrix.toString("===Confusion Matrix==="));

      } catch (Exception e) {
         Assert.fail("Exception reading file" + testFile);
         e.printStackTrace();
      }

   }

}
