package airldm2.core.datatypes.relational;

import java.util.Vector;
import java.net.URI;
import java.io.PrintStream;

import airldm2.constants.Constants;

/**
 * 
 * @author neeraj
 * 
 */
public class ColumnDescriptor {

    public static String DEFAULT_MISSING_VALUE = "?";

    /* */
    private static Integer DEFAULT_SIZE = 1024;
    private String columnName;

    /* How big an columnValue can be if it is String/varchar */
    private static Integer columnSize = DEFAULT_SIZE;

    private final String dataType = "STRING";
    // TODO: This should be enum. Currently Everything is String

    /* indicates whether it is an Attribute Value Hierarchy */
    private final boolean isAVH = false;

    /*
     * Possible Values if it has an hierarchy associated with it. Even
     * multi-nominal values is a default hierarchy
     */
    private Vector<String> possibleValues; // vector as we will be
    // requiring
    // order

    /**
     * How a missing value is represented
     */
    private String missingValue = DEFAULT_MISSING_VALUE;
    /*
     * Since concepts in an ontology are URI, add this to possible values to
     * construct concepts
     */
    private URI base;

    /**
     * gets the Index of a value in the possible values for this column
     * 
     * @param value
     * @return the index found. A -1 is returned if not found or not applicable
     */
    public int getIndex(String value) {

	if (getPossibleValues() == null)
	    return -1;
	int index = -1;
	for (int i = 0; i < getPossibleValues().size(); i++) {
	    if (getPossibleValues().get(i).equals(value)) {
		index = i;
		break;
	    }
	}
	return index;
    }

    /**
     * returns the number of possible values it can take returns -1 if not
     * applicable
     * 
     * @return
     */
    public int getNumValues() {
	if (getPossibleValues() == null)
	    return -1;
	return getPossibleValues().size();
    }

    /**
     * @return the columnName
     */
    public String getColumnName() {
	return columnName;
    }

    /**
     * 
     * @param name
     * 
     */
    public void setColumnName(String name) {
	this.columnName = name;
    }

    /**
     * @return the possibleValues that this column can take
     */
    public Vector<String> getPossibleValues() {
	return possibleValues;
    }

    public void setPossibleValues(Vector<String> values) {
	this.possibleValues = values;

    }

    public void setPossibleValues(Vector<String> values, String missingValue) {
	this.possibleValues = values;
	this.missingValue = missingValue;

    }

    /**
     * 
     * @return how a missing value is represented
     */
    public String getMissingValue() {
	return this.missingValue;
    }

    /**
     * A utility function to return comma seperated possible values
     * 
     * @return
     */
    public String getCommaSeperatedPossibleValues() {
	String result = "";
	for (String name : possibleValues) {
	    result += name + Constants.COMMA;
	}
	result = airldm2.util.Utils.removeTrailing(result, Constants.COMMA);
	return result;
    }

    public void dump(PrintStream out) {
	out.println("name=" + columnName);
	out.println("values=" + getCommaSeperatedPossibleValues());
    }

    /**
     * @param columnSize
     *                the columnSize to set
     */
    public void setColumnSize(Integer columnSize) {
	ColumnDescriptor.columnSize = columnSize;
    }

    /**
     * @return the columnSize
     */
    public Integer getColumnSize() {
	return columnSize;
    }
}
