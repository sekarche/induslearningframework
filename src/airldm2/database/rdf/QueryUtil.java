package airldm2.database.rdf;

import static airldm2.util.StringUtil.angleBracket;
import static airldm2.util.StringUtil.triple;

import java.util.List;

import org.openrdf.model.URI;

import airldm2.core.rl.PropertyChain;

public class QueryUtil {

   public static String createValueChain(PropertyChain propChain, String head, VarFactory varFactory) {
      StringBuilder b = new StringBuilder();
      
      List<URI> props = propChain.getList();
      b.append(triple(head, angleBracket(props.get(0)), varFactory.next()));
      for (int i = 1; i < props.size(); i++) {
         b.append(triple(varFactory.current(), angleBracket(props.get(i)), varFactory.next()));
      }

      return b.toString();
   }
   
}
