package airldm2.util;

import java.util.List;

import org.openrdf.model.URI;

/**
 * Some commonly used functions
 * 
 * @author neeraj
 * 
 */
public class StringUtil {
   
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
   
   public static String toCSV(double[] as) {
      StringBuilder b = new StringBuilder();
      
      for (int i = 0; i < as.length - 1; i++) {
         b.append(as[i]);
         b.append(",");
      }
      b.append(as[as.length - 1]);
      
      return b.toString();
   }
   
   public static String toCSV(List<String> as) {
      StringBuilder b = new StringBuilder();
      
      for (int i = 0; i < as.size() - 1; i++) {
         b.append(as.get(i));
         b.append(",");
      }
      b.append(as.get(as.size() - 1));
      
      return b.toString();
   }
   
}
