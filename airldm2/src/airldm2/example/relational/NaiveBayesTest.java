/**
 * 
 */
package airldm2.example.relational;

import java.util.Vector;

import airldm2.classifiers.bayes.NaiveBayesClassifier;
import airldm2.core.LDInstance;
import airldm2.core.LDInstances;
import airldm2.core.SSDataSource;
import airldm2.core.datatypes.relational.ColumnDescriptor;
import airldm2.core.datatypes.relational.RelationalDataSource;
import airldm2.core.datatypes.relational.SingleRelationDataDescriptor;

/**
 * @author neeraj
 * 
 */
public class NaiveBayesTest {

   /**
    * @param args
    */
   public static void main(String[] args) {
      try {

         String tableName = "votes2";

         /**
          * set the SufficentStatistic (SS) DataSource. It gets the
          * connection information from config files . The table should
          * exist in the database
          * 
          */
         SSDataSource dataSource = new RelationalDataSource(tableName);

         /* A way to get the descriptor of the columns in the table */
         Vector<ColumnDescriptor> columns = getAttributeDesc();

         /**
          * Create A desciptor for the SSDatasource
          */
         SingleRelationDataDescriptor desc = new SingleRelationDataDescriptor(
               tableName, columns);

         // Create a Large DataSet Instance and set its
         // descriptor and source
         LDInstances instances = new LDInstances();
         instances.setDesc(desc);
         instances.setDataSource(dataSource);

         // create a Naive Bayes Class
         NaiveBayesClassifier naiveBayes = new NaiveBayesClassifier();
         naiveBayes.buildClassifier(instances);

         // create programatically an instance to test
         Vector<String> instanceAttributeValues = new Vector<String>();
         instanceAttributeValues.add("y");
         instanceAttributeValues.add("y");

         LDInstance testInstance = new LDInstance(desc,
               instanceAttributeValues, false);

         double index = naiveBayes.classifyInstance(testInstance);
         int resIndex = new Double(index).intValue();

         ColumnDescriptor labelAttribute = desc.getTableDesc().getColumns()
               .lastElement();

         String predictedClass = labelAttribute.getPossibleValues().get(
               resIndex);
         System.out.println("predicted class=" + predictedClass);

      } catch (Exception e) {
         System.out.println("Error: " + e.getMessage());
         e.printStackTrace();
      }

   }

   private static Vector<ColumnDescriptor> getAttributeDesc() {
      Vector<ColumnDescriptor> columns = new Vector<ColumnDescriptor>();

      Vector<String> values = new Vector<String>();
      values.add("y");
      values.add("n");
      values.add("?");

      ColumnDescriptor temp = new ColumnDescriptor();
      temp.setColumnName("crime");
      temp.setPossibleValues(values);

      ColumnDescriptor temp2 = new ColumnDescriptor();
      temp2.setColumnName("immigration");
      temp2.setPossibleValues(values);

      ColumnDescriptor label = new ColumnDescriptor();
      label.setColumnName("Class");
      Vector<String> lValues = new Vector<String>();
      lValues.add("democrat");
      lValues.add("republican");
      label.setPossibleValues(lValues);

      columns.add(temp);
      columns.add(temp2);
      columns.add(label);

      return columns;

   }
}
