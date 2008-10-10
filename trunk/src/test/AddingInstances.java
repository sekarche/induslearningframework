package test;

import airldm2.core.LDInstances;
import airldm2.util.SimpleArffFileReader;

public class AddingInstances {
   public static void main(String[] args) throws Exception {
      String base = System.getProperty("user.dir");
      String trainFileName = base + "/sample/exp.arff";
      /*
       * SELECT count( * ) FROM `votes_exp` WHERE
       * `water-project-cost-sharing` = 'y' AND `physician-fee-freeze` =
       * 'y'
       */
      String relationName = "votes_exp";
      String attribName1 = "water-project-cost-sharing";
      String attribValue1 = "y";
      String attribName2 = "physician-fee-freeze";
      String attribValue2 = "y";
      String countQuery = "SELECT count( * ) FROM votes_exp;";

      String testQuery = "SELECT count(*) FROM " + relationName + " WHERE `"
            + attribName1 + "`='" + attribValue1 + "' AND `" + attribName2
            + "`='" + attribValue2 + "'";

      SimpleArffFileReader readTrain;
      for (int i = 0; i < 10; i++) {
         readTrain = new SimpleArffFileReader(trainFileName);
         long startReadingTime = System.currentTimeMillis();
         LDInstances instances = readTrain.getLDInstances(false);
         long endReadingTime = System.currentTimeMillis();

      }
   }
}