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

/**
 * TODO Write class description here.
 * 
 * @author neeraj (TODO Write email id here)
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
   public void testNBWithOptionsDB() {

      String[] options = { "-b", "-indus", "-trainTable", "MOVIETABLE",
            "-testFile", "indus_example/MovieTest.arff" };
      NaiveBayesClassifier classifier = new NaiveBayesClassifier();
      try {
         String out = Evaluation.evaluateModel(classifier, options);
         System.out.println(out);

      } catch (Exception e) {
         e.printStackTrace();
         Assert.fail();

      }

   }

}
