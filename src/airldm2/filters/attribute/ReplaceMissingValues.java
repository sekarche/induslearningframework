package airldm2.filters.attribute;

import weka.filters.UnsupervisedFilter;

/**
 * 
 * @author neeraj
 * 
 */
public class ReplaceMissingValues implements UnsupervisedFilter {

    /**
     * Assigns the missing values for an attribute proportionally
     * 
     * @param counts
     * @param numberInstances
     * @return
     */
    public static double[][][] SpreadMissingValues(double[][][] counts,
	    int numberInstances) throws Exception {
	/*
	 * It goes: [attribute name][class value][attribute value]
	 */

	double[] numberMissingInstanceAttributes = getNumberOfMissingValuesForAttribute(
		counts, numberInstances);
	for (int i = 0; i < counts.length; i++) {
	    for (int j = 0; j < counts[i].length; j++) {
		for (int k = 0; k < counts[i][j].length; k++) {
		    double numberofInstancesThisAttribute = numberInstances
			    - numberMissingInstanceAttributes[i];

		    counts[i][j][k] += counts[i][j][k]
			    * (numberMissingInstanceAttributes[i] / numberofInstancesThisAttribute);
		}
	    }
	}

	return counts;

    }

    /**
     * 
     * @param counts
     * @param numberInstances
     * @return an array containing number of instances missing for a given
     *         attribute(indexed by attribute position)
     */

    private static double[] getNumberOfMissingValuesForAttribute(
	    double[][][] counts, int numberInstances) throws Exception {
	int numberAttributes = counts.length;
	double[] missingAttributeCount = new double[numberAttributes];
	double numberAttributeInstances = 0;
	for (int i = 0; i < counts.length; i++) {
	    numberAttributeInstances = 0; // initialize for curr attribute
	    for (int j = 0; j < counts[i].length; j++) {
		for (int k = 0; k < counts[i][j].length; k++) {
		    numberAttributeInstances += counts[i][j][k];
		}
		missingAttributeCount[i] = numberInstances
			- numberAttributeInstances;
		if (missingAttributeCount[i] < 0.0) {
		    throw new Exception("The Attribute at index " + i
			    + " has Instances greater that total instancess");
		}

	    }
	}

	return missingAttributeCount;

    }
}
