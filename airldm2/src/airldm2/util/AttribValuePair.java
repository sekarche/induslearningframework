/**
 * A Utility Class to Set Name Value Pairs
 */
package airldm2.util;

/**
 * @author neeraj
 * 
 */
public class AttribValuePair {
	private String name;
	String value;

	/**
	 * @param name
	 *            the name to set
	 */
	public void setAttribName(String name) {
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getAttribName() {
		return name;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setAttribValue(String value) {
		this.value = value;
	}

	/**
	 * @return the value
	 */
	public String getAttribValue() {
		return value;
	}

	public AttribValuePair(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public AttribValuePair() {
	}

}
