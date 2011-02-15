package test;

import java.util.Vector;

import airldm2.classifiers.rl.RelationalBayesianClassifier;
import airldm2.core.LDInstance;
import airldm2.core.LDInstances;
import airldm2.core.SSDataSource;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;

   
public class RelationalBayesianClassifierTest {
   
   public static void main(String[] args) throws Exception {
      String context = ":default";

      /**
       * set the SufficentStatistic (SS) DataSource. It gets the
       * connection information from config files . The table should
       * exist in the database
       * 
       */
      SSDataSource dataSource = new RDFDataSource(context);

      Vector<RbcAttribute> attributes = getAttributeDesc();
      RDFDataDescriptor desc = new RDFDataDescriptor(context, attributes);
      
      

      // Create a Large DataSet Instance and set its
      // descriptor and source
      LDInstances instances = new LDInstances();
      instances.setDesc(desc);
      instances.setDataSource(dataSource);

      RelationalBayesianClassifier rbc = new RelationalBayesianClassifier();
      rbc.buildClassifier(instances);

      // create programatically an instance to test
      Vector<String> instanceAttributeValues = new Vector<String>();
      instanceAttributeValues.add("y");
      instanceAttributeValues.add("y");

      LDInstance testInstance = new LDInstance(desc,
            instanceAttributeValues, false);

      double index = rbc.classifyInstance(testInstance);
//      int resIndex = new Double(index).intValue();
//
//      ColumnDescriptor labelAttribute = desc.getTableDesc().getColumns()
//            .lastElement();
//
//      String predictedClass = labelAttribute.getPossibleValues().get(
//            resIndex);
      System.out.println("predicted class=???");
   }

   private static Vector<RbcAttribute> getAttributeDesc() {
      return null;
   }

}
