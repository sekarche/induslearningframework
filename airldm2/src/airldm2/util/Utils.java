package airldm2.util;

/**
 * Some commonly used functions
 * 
 * @author neeraj
 * 
 */
public class Utils {
    public static String removeTrailing(String st, String trailer) {
	String s = st.substring(0, st.lastIndexOf(trailer));
	return s;
    }
}
