/**
 * DecisionTreeTests.java<br>
 * Basic Tests For Decision Trees
 * 
 * $Header: $
 */

package test;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import weka.classifiers.evaluation.ConfusionMatrix;
import weka.core.Utils;
import airldm2.classifiers.Evaluation;
import airldm2.classifiers.trees.Id3SimpleClassifier;
import airldm2.core.LDInstances;
import airldm2.core.LDTestInstances;
import airldm2.util.SimpleArffFileReader;

/**
 * Junit Tests for DecisionTree
 * 
 * @author neeraj (neeraj.kaul@gmail.com,neeraj@cs.iastate.edu)
 * @since Feb 9, 2009
 * @version $Date: $
 */
public class DecisionTreeTests {

   /**
    * @throws java.lang.Exception
    */
   @Before
   public void setUp() throws Exception {
   }

   /**
    * @throws java.lang.Exception
    */
   @After
   public void tearDown() throws Exception {
   }

   @Test
   public void testDecisionTreeReadingFromArff() {
      String[] options = { "-a", "-trainFile", "sample/HouseVotesTrain.arff",
            "-testFile", "sample/HouseVotesTest.arff" };
      Id3SimpleClassifier classifier = new Id3SimpleClassifier();

      try {
         String res = Evaluation.evaluateModel(classifier, options);
         System.out.println(res);
      } catch (Exception e) {
         System.out.println("Error(1001):" + e.getMessage());
         e.printStackTrace();
      }
   }

   @Test
   public void testDecisionTreeWithDatainDB() {
      /* Ensure votes_train is in DB */
      String[] options = { "-b", "-trainTable", "votes_train", "-testFile",
            "sample/HouseVotesTest.arff" };

      Id3SimpleClassifier classifier = new Id3SimpleClassifier();

      try {
         String res = Evaluation.evaluateModel(classifier, options);
         System.out.println(res);
      } catch (Exception e) {
         System.out.println("Error(1001):" + e.getMessage());
         e.printStackTrace();
      }
   }

   @Test
   public void testDecisionTreeWithDatainDBSanityMissingValueOption() {
      /* Ensure votes_train is in DB */
      String[] options = { "-b", "-?", "-trainTable", "votes_train",
            "-testFile", "sample/HouseVotesTest.arff" };

      Id3SimpleClassifier classifier = new Id3SimpleClassifier();

      try {
         String res = Evaluation.evaluateModel(classifier, options);
         System.out.println(res);
      } catch (Exception e) {
         System.out.println("Error(1001):" + e.getMessage());
         e.printStackTrace();
      }
   }

   @Test
   public void testDecisionTreeReadingFromArffMissingValues() {
      String[] options = { "-a", "-?", "-trainFile", "sample/HouseVotes.arff",
            "-testFile", "sample/HouseVotesTest.arff" };
      Id3SimpleClassifier classifier = new Id3SimpleClassifier();

      try {
         String res = Evaluation.evaluateModel(classifier, options);
         System.out.println(res);
      } catch (Exception e) {
         System.out.println("Error(1001):" + e.getMessage());
         e.printStackTrace();
      }
   }

   /**
    * Weather DataSet
    */
   @Test
   public void testDecisonTreeWithTrainInArffMultipleValues() {
      String[] options = { "-a", "-?", "-trainFile",
            "sample/weather_nominal.arff", "-testFile",
            "sample/weather_nominalTest.arff" };

      try {

         String trainFile = Utils.getOption("trainFile", options);
         String testFile = Utils.getOption("testFile", options);
         testDecisionTreeWithTrainInArff(trainFile, testFile, options);
      } catch (Exception e) {
         Assert.fail();
         e.printStackTrace();
      }

      /*
       * Alternate way to test above is
       * 
       * Id3SimpleClassifier classifier = new Id3SimpleClassifier();
       * 
       * String res = Evaluation.evaluateModel(classifier, options);
       * 
       * Use the above format to show code fragment to build LDInstances
       */

   }

   @Test
   public void testDecisionTreeEvaluationOptions() {

      /**
       * Decision Tree test which uses options to figure that the training
       * data is in database
       */
      try {

         Id3SimpleClassifier classifier = new Id3SimpleClassifier();

         // String[] options = { "-b", "-trainTable", "votes_train",
         // "-testFile",
         // "sample/HouseVotesTrain.arff" };

         String[] options = { "-b", "-?", "-trainTable", "weather",
               "-testFile", "sample/weather_nominalTest.arff " };

         String res = Evaluation.evaluateModel(classifier, options);
         // Print Confusion Matrix
         System.out.println(res);
         // Print Decision Tree
         System.out.println(" \n " + classifier.toString());

      } catch (Exception e) {
         e.printStackTrace();
         Assert.fail();

      }

   }

   private void testDecisionTreeWithTrainInArff(String trainFile,
         String testFile, String[] options) {
      /**
       * Decision Tree when the training file is in Arff.
       * 
       */

      Id3SimpleClassifier classifier = new Id3SimpleClassifier();

      try {
         SimpleArffFileReader readTrain = new SimpleArffFileReader(trainFile);
         SimpleArffFileReader readTest = new SimpleArffFileReader(testFile);
         LDInstances trainData = readTrain.getLDInstances(true);
         LDTestInstances testData = readTest.getTestInstances();
         ConfusionMatrix matrix = Evaluation.evlauateModel(classifier,
               trainData, testData, options);
         System.out.println(matrix.toString("===Confusion Matrix==="));

         System.out.println(" \n " + classifier.toString());

      } catch (Exception e) {

         Assert.fail("Error reading instances or buildDing classifier:"
               + e.getMessage());
         e.printStackTrace();
      }

   }
}