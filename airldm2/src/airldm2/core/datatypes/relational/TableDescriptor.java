package airldm2.core.datatypes.relational;

import java.util.Vector;

/**
 * 
 * @author neeraj
 * 
 */
public class TableDescriptor {

    private String tableName;
    /*
     * A list of columns for the table. It is a vector since we will be using
     * Order We assume it is a unique list of columns
     */
    private Vector<ColumnDescriptor> columns = new Vector<ColumnDescriptor>();

    /**
     * @param tableName
     *                the tableName to set
     */
    public void setTableName(String tableName) {
	this.tableName = tableName;
    }

    /**
     * @return the tableName
     */
    public String getTableName() {
	return tableName;
    }

    /**
     * @param columns
     *                the columns to set
     */
    public void setColumns(Vector<ColumnDescriptor> columns) {
	this.columns = columns;
    }

    /**
     * @return the columns
     */
    public Vector<ColumnDescriptor> getColumns() {
	return columns;
    }

    /**
     * constructor
     * 
     * @param tableName
     * @param columns
     */
    public TableDescriptor(String tableName, Vector<ColumnDescriptor> columns) {
	this.tableName = tableName;
	this.columns = columns;

    }

    public TableDescriptor() {

    }
}
