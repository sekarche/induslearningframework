/**
 * IndusDataSourceTest.java<br>
 * TODO Write description for IndusDataSourceTest.java.
 * 
 * $Header: $
 */

package test;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import airldm2.classifiers.Evaluation;
import airldm2.classifiers.bayes.NaiveBayesClassifier;
import airldm2.classifiers.trees.Id3SimpleClassifier;

/**
 * TODO Write class description here.
 * 
 * @author neeraj (neeraj@cs.iastate.edu, neeraj.kaul@gmail.com)
 * @since Oct 28, 2008
 * @version $Date: $
 */
public class IndusDataSourceTest {

   /**
    * @throws java.lang.Exception
    */
   @Before
   public void setUp() throws Exception {
   }

   @Test
   public void testNBWithOptionsDB2() {

      String[] options = { "-b", "-indus", "-indus_base", "indus_example-2",
            "-trainTable", "details", "-testFile",
            "indus_example-2/MovieTest.arff" };
      NaiveBayesClassifier classifier = new NaiveBayesClassifier();
      try {
         String out = Evaluation.evaluateModel(classifier, options);
         System.out.println(out);

      } catch (Exception e) {
         e.printStackTrace();
         Assert.fail();

      }

   }

   // @Test
   public void testDecisionTreeWithDatainDB() {
      /* Ensure votes_train is in DB */
      String[] options = { "-b", "-indus", "-indus_base", "indus_example-2",
            "-trainTable", "details", "-testFile",
            "indus_example-2/MovieTest.arff" };

      Id3SimpleClassifier classifier = new Id3SimpleClassifier();

      try {
         String res = Evaluation.evaluateModel(classifier, options);
         System.out.println(res);
      } catch (Exception e) {
         System.out.println("Error(1001):" + e.getMessage());
         e.printStackTrace();
         Assert.fail();
      }
   }
}
