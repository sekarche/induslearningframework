/**
 * 
 */
package airldm2.core;

/**
 * @author neeraj
 * 
 * This is an interface which every data source must implement describing the
 * data it contains
 * 
 */
public interface DataDescriptor {

    /* Returns a name of the Data for which this is the descriptor */
    public String getDataName();

    public String getProperty(String key);

    public int getAttributeCount();
    public String[] getClassLabels();
}
