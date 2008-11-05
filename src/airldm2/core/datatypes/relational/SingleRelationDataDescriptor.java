package airldm2.core.datatypes.relational;

import java.util.Hashtable;
import java.util.Vector;

import airldm2.core.DataDescriptor;

public class SingleRelationDataDescriptor implements DataDescriptor {

   private TableDescriptor relation;

   private Hashtable<String, String> props = new Hashtable<String, String>();

   int numberColumns = -1;

   public String getDataName() {
      return relation.getTableName();
   }

   public TableDescriptor getTableDesc() {
      return relation;
   }

   public SingleRelationDataDescriptor(String tableName,
         Vector<ColumnDescriptor> columns) {
      relation = new TableDescriptor(tableName, columns);

      numberColumns = (columns != null) ? columns.size() : -1;
   }

   public SingleRelationDataDescriptor() {
   }

   public String getProperty(String key) {
      return props.get(key);
   }

   public void setProperty(String key, String value) {
      props.put(key, value);
   }

   public int getAttributeCount() {
      return numberColumns;
   }

   public ColumnDescriptor getClassLabelDescriptor() {
      // TODO: All Access to ClassLabel Descriptor should be through this
      return relation.getColumns().lastElement();
   }

   public String[] getClassLabels() {
      return getClassLabelDescriptor().getPossibleValuesAsArray();
   }

}
