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
   
   public static String[] trim(String[] strs) {
      for (int i = 0; i < strs.length; i++) {
         if (strs[i] != null) {
            strs[i] = strs[i].trim();
         }
      }
      return strs;
   }

   public static String angleBracket(URI uri) {
      return angleBracket(uri.toString());
   }
   
   public static String angleBracket(String uri) {
      return new StringBuilder().append("<").append(uri).append(">").toString();
   }

   public static String triple(String sub, String prop, String obj) {
      return new StringBuilder()
         .append(sub)
         .append(" ")
         .append(prop)
         .append(" ")
         .append(obj)
         .append(" . ")
         .toString();
   }
   
   public static String makeContextPart(String context) {
      if (context == null) return "";
      else return "FROM " + angleBracket(context);
   }
   
}
