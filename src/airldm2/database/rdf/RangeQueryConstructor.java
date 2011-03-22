package airldm2.database.rdf;

import static airldm2.util.StringUtil.angleBracket;
import static airldm2.util.StringUtil.makeContextPart;
import static airldm2.util.StringUtil.triple;

import java.util.List;

import org.openrdf.model.URI;

import airldm2.core.rl.RbcAttribute;


public class RangeQueryConstructor {

   private static final String TARGET_VAR = "?tar";
   private static final String LAST_VAR_PATTERN = "%lastVar%";
   private static final String CONTEXT_PATTERN = "%context%";
   private static final String TYPE_PATTERN = "%type%";
   private static final String QUERY_HEADER = "SELECT DISTINCT " + LAST_VAR_PATTERN + " " + CONTEXT_PATTERN + " WHERE { " + TARGET_VAR + " rdf:type " + TYPE_PATTERN + " . ";
    
   private String mContextPart;
   private URI mType;
   private RbcAttribute mAttribute;
   private VarFactory mVarFactory;
   
   public RangeQueryConstructor(String context, URI type, RbcAttribute attribute) {
      mContextPart = makeContextPart(context);
      mType = type;
      mAttribute = attribute;
      mVarFactory = new VarFactory();
   }

   public String createQuery() {
      StringBuilder b = new StringBuilder();
      
      String chain = createValueChain(mAttribute);
      b.append(QUERY_HEADER.replace(LAST_VAR_PATTERN, mVarFactory.current())
                           .replace(CONTEXT_PATTERN, mContextPart)
                           .replace(TYPE_PATTERN, angleBracket(mType)));
      b.append(chain);
      b.append("} ORDER BY ").append(mVarFactory.current());
      return b.toString();
   }
   
   private String createValueChain(RbcAttribute att) {
      StringBuilder b = new StringBuilder();
      
      List<URI> props = att.getProperties();
      b.append(triple(TARGET_VAR, angleBracket(props.get(0)), mVarFactory.next()));
      for (int i = 1; i < props.size(); i++) {
         b.append(triple(mVarFactory.current(), angleBracket(props.get(i)), mVarFactory.next()));
      }

      return b.toString();
   }
   
}
