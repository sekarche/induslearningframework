package airldm2.core;

import java.util.Vector;

import airldm2.core.datatypes.relational.ColumnDescriptor;
import airldm2.core.datatypes.relational.SingleRelationDataDescriptor;
import airldm2.exceptions.RTConfigException;
import airldm2.constants.Constants;

/**
 * 
 * @author neeraj
 * 
 * Represents a Single Instance of the Data. It is assumed a Single
 * Instance can be stored in memory unlike LDInstance
 */
public class LDInstance {

   /**
    * The Data Descriptor for this instance
    */
   DataDescriptor desc;

   /*
    * The Values of the various attributes for this DataDescriptor.
    * Everything is stored as a String. It can be converted to appropriate
    * types based on the DataDesc
    */
   Vector<String> values;

   /**
    * Whether this is a labeled instance. If this is labeled, the last
    * value is assumed to be class label.
    */
   boolean labeled;

   int[] locations;

   /**
    * 
    * @param desc
    * @param values
    * @param labeled if false the size of values is one less than number of
    * attributed in the desc
    * @throws RTConfigException
    */
   public LDInstance(DataDescriptor desc, Vector<String> values, boolean labeled)
         throws RTConfigException {
      this.desc = desc;
      this.values = values;
      this.labeled = labeled;
      if (!okayConfig()) {
         throw new RTConfigException(100,
               "The Descriptor and value for the instance are not compataible");
      }

      // TODO: some preliminary checking
      // Verify that the given values are those excepted by the descriptor
   }

   public String getClassLabel() throws Exception {
      if (labeled) {
         // currently last value is class label
         // TODO Add support to declare any attribute as class label
         return values.lastElement();
      } else
         throw new Exception("Unlabeled Instance. Can't return Class label");
   }

   /**
    * 
    * @return a comma seperated list of strings representing this instance
    */

   public String getStringRepresentation() {
      String result = "";
      for (String val : values) {
         result += val + Constants.COMMA;
      }
      result = airldm2.util.Utils.removeTrailing(result, Constants.COMMA);
      return result;
   }

   private boolean okayConfig() {
      boolean okay = true;
      int attribSize = desc.getAttributeCount();
      int valueSize = values.size();
      ;
      if (labeled) {
         if (attribSize != valueSize) {
            okay = false;
         }

      } else {

         if ((attribSize - 1) != (valueSize)) {
            okay = false;
         }
      }

      // TODO: Verify that the given values are those excepted by the
      // descriptor
      return okay;

   }

   /**
    * Returns the array , where each value array corresponding to the
    * location of the current value for that attribute in posssible values
    * that attribute can take
    * 
    * @return
    */
   public int[] getLocation() {
      if (locations != null) {
         return locations;
      }

      // Will Have to change if we use other descriptors(Cast Will fail)
      // Change function to take parameters. No need to over engineer
      // currently
      SingleRelationDataDescriptor tableDesc = (SingleRelationDataDescriptor) desc;
      Vector<ColumnDescriptor> cols = tableDesc.getTableDesc().getColumns();

      // if labeled, the values should also contain class label(assumed to
      // be
      if (labeled) {
         locations = new int[cols.size()];
      } else {
         locations = new int[cols.size() - 1];
      }

      ColumnDescriptor currColumn;
      String currValue;
      // size of values is one less or equal to size of values. So use that
      // as
      // index
      for (int i = 0; i < values.size(); i++) {
         currValue = values.get(i);
         currColumn = cols.get(i); // size of columns is greater that or
         // equal to size of values
         locations[i] = currColumn.getIndex(currValue);
      }

      return locations;

   }

   public String getValue(int index) {
      return values.get(index);
   }
   
   /**
    * 
    * @param cd Column Descriptor for which the value is desired
    * @return the value of the attribute (described by cd) of the LDinstance
    *
    */

   public String getValue(ColumnDescriptor cd) {
      SingleRelationDataDescriptor tableDesc = (SingleRelationDataDescriptor) desc;
      Vector<ColumnDescriptor> cols = tableDesc.getTableDesc().getColumns();
      ColumnDescriptor currColumn;
      int index = -1;
      for (int i = 0; i < cols.size(); i++) {
         currColumn = cols.get(i);
         if ((currColumn.getColumnName()).equals(cd.getColumnName())) {
            index = i;
            break;
         }
      }
      if (index != -1)
         return values.get(index);
      else
         return null;
   }

}