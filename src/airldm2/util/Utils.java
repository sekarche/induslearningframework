package airldm2.util;

import org.openrdf.model.URI;

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

   public static String angleBracket(URI uri) {
      return angleBracket(uri.toString());
   }
   
   public static String angleBracket(String uri) {
      return new StringBuilder().append("<").append(uri).append(">").toString();
   }
   
}
