/**
 * IndusDataSource.java<br>
 * TODO Write description for IndusDataSource.java.
 * 
 * $Header: $
 */

package airldm2.core.datatypes.relational;

import java.net.URISyntaxException;
import java.util.Vector;

import org.iastate.ailab.qengine.core.Init;

import airldm2.exceptions.RTConfigException;

/**
 * Creates an IndusDataSource that automatically constructs the descriptor
 * 
 * @author neeraj (neeraj.kaul@gmail.com, neeraj@cs.iastate.edu)
 * @since Oct 26, 2008
 * @version $Date: $
 */
public class IndusDataSourceWithDesc extends IndusDataSource {

   SingleRelationDataDescriptor desc;

   // QueryEngine engine;

   // initialize to -1 to show it has not been calculated
   // int numberOfInstances = -1;

   /**
    * Constructor with base pointing to the directory containing indus.conf
    * It automatically constructs the descriptor from the view
    * 
    * @param base
    * @param classLabelName Which attribute is the class label
    * @throws Exception
    * 
    */
   public IndusDataSourceWithDesc(String base, String classLabelName)
         throws Exception {

      super(base); // call constructor with null desc. It has to be
      // first call in method
      // now construct and set the descriptor below
      this.desc = getAirlDmDescFromIndusDesc(Init._this()
            .getUserViewDataSourceDescriptor(), classLabelName);

      // automatically get the relationName from desc
      setRelationName(desc.getTableDesc().getTableName());
   }

   public static SingleRelationDataDescriptor getAirlDmDescFromIndusDesc(
         org.iastate.ailab.qengine.core.datasource.DataSourceDescriptor indusDesc,
         String classLabelColumn) throws RTConfigException {
      // TODO: Move to a util function, it converts indus data descriptor
      // to airldm data descriptor

      Vector<ColumnDescriptor> columns = new Vector<ColumnDescriptor>();
      ColumnDescriptor currcolumnDesc = new ColumnDescriptor();
      ColumnDescriptor classLabelColumnDesc = new ColumnDescriptor();

      org.iastate.ailab.qengine.core.reasoners.interfaces.DataSchemaResource indusDataSchemaResource;

      String dataSourceName = indusDesc.getDSName();
      // currently only single table
      String tableName = indusDesc.getDSTables()[0];
      String currColumnName;
      Vector<String> columnNames = indusDesc.getAllColumnNames();

      for (int i = 0; i < columnNames.size(); i++) {
         currColumnName = columnNames.elementAt(i);
         indusDataSchemaResource = new org.iastate.ailab.qengine.core.reasoners.impl.DataSchemaResourceImpl(
               dataSourceName, tableName, currColumnName);
         String base = indusDesc.getColumnDescriptor(indusDataSchemaResource)
               .getProperty("base");

         // set the currrent column descriptor for airldm
         currcolumnDesc.setColumnName(currColumnName);

         try {
            currcolumnDesc.setBase(new java.net.URI(base));
         } catch (URISyntaxException e) {
            throw new airldm2.exceptions.RTConfigException(100,
                  " The base associated with column " + currColumnName
                        + " cannot be converted to URI. The value of base is: "
                        + base);

         }

         currcolumnDesc.setPossibleValues(indusDesc.getColumnDescriptor(
               indusDataSchemaResource).getPossibleValuesAsinDB());
         if (currColumnName.equals("classLabelColumn")) {
            // this is the class label column, should be the last column
            classLabelColumnDesc = currcolumnDesc;
         } else {
            columns.add(currcolumnDesc);
         }

      }

      columns.add(classLabelColumnDesc);
      SingleRelationDataDescriptor desc = new SingleRelationDataDescriptor(
            tableName, columns);
      return desc;

   }
}
