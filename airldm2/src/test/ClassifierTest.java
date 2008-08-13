/**
 * NBClassifierTest.java<br>
 * TODO Write description for NBClassifierTest.java.
 *
 * $Header: $
 */

package test;


import airldm2.core.datatypes.relational.SingleRelationDataDescriptor;
import org.junit.Before;
import org.junit.Test;
import junit.framework.Assert;
import airldm2.classifiers.bayes.NaiveBayesClassifier;
import airldm2.core.LDInstances;
import airldm2.core.LDTestInstances;
import airldm2.core.SSDataSource;
import airldm2.core.datatypes.relational.RelationalDataSource;
import  airldm2.util.SimpleArffFileReader;
import airldm2.classifiers.Evaluation;
import airldm2.classifiers.IlfConfusionMatrix;

import weka.classifiers.evaluation.ConfusionMatrix;
import weka.core.Utils;

/**
 * TODO Write class description here.
 *
 * @author neeraj (TODO Write email id here)
 * @since Aug 12, 2008
 * @version $Date: $
 */
public class  ClassifierTest{
   /**
    * @throws java.lang.Exception
    */
   @Before
   public void setUp() throws Exception {
   }
   
   //@Test
   public void testNBWithOptionsArff(){
      
      String[] options= {"-a", "-?", "-trainFile", "sample/HouseVotesTrain.arff", "-testFile" ,"sample/HouseVotesTrain.arff"};
      try {
          
         String trainFile = Utils.getOption("trainFile", options);
         String testFile = Utils.getOption("testFile", options);
         this.testNBWithTrainInArff(trainFile, testFile, options);
      } catch (Exception e) {
         Assert.fail();
         e.printStackTrace();
      }
      
   }
   
   
   //@Test
   public void testNBWithOptionsDB(){
      
      String[] options= {"-b",  "-trainTable", "votes_train", "-testFile" ,"sample/HouseVotesTrain.arff"};
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
      String[] options= {"-b",  "-trainTable", "votes_train", "-testFile" ,"sample/HouseVotesTrain.arff"};
       
      String res = Evaluation.evaluateModel(classifier, options);
      System.out.println(res);
      } catch (Exception e) {
         Assert.fail();
         e.printStackTrace();
      }
   }
   

   
   private void testNBWithTrainInArff(String trainFile, String testFile,String[] options){
      NaiveBayesClassifier classifier = new NaiveBayesClassifier();
      try {
         SimpleArffFileReader readTrain = new SimpleArffFileReader(trainFile);
         SimpleArffFileReader readTest = new SimpleArffFileReader(testFile);
         LDInstances trainData = readTrain.getLDInstances(true);
         LDTestInstances  testData    = readTest.getTestInstances();
         
         IlfConfusionMatrix matrix = Evaluation.evlauateModel(classifier, trainData, testData, options);
         matrix.toString();
         
      } catch (Exception e) {
         Assert.fail("Error reading instances or buildDing classifier");
         e.printStackTrace();
      }
      
     
      
      
   }
   
   
 
   
   private void testNBWithTrainInDB2(String testFile, String trainTableName, String[] options){
      
      NaiveBayesClassifier classifier = new NaiveBayesClassifier();
     
     
      
      SingleRelationDataDescriptor  desc = null;
     
     
     
      try {
         SimpleArffFileReader readTest = new SimpleArffFileReader(testFile);
         LDTestInstances testInst = readTest.getTestInstances();
         desc = (SingleRelationDataDescriptor )testInst.getDesc();
         
         SSDataSource dataSource = new RelationalDataSource(trainTableName);
         // Create a Large DataSet Instance and set its
         // descriptor and source
         LDInstances trainData = new LDInstances();
         trainData.setDesc(desc);
         trainData.setDataSource(dataSource);
         
         ConfusionMatrix matrix = Evaluation.evlauateModel2(classifier, trainData, testInst, options);
         System.out.println(matrix.toString("===Confusion Matrix==="));
         
         
      } catch (Exception e) {
         Assert.fail("Exception reading file" + testFile);
         e.printStackTrace();
      }
      
     
    


      }
      
      
   
   @Test
   public void testDescionTree() {
      //TODO  Implementation
      Assert.assertEquals(false, true);
   }
   

}
