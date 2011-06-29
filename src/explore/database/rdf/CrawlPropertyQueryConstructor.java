package explore.database.rdf;

import static airldm2.util.StringUtil.angleBracket;
import static airldm2.util.StringUtil.makeContextPart;
import static airldm2.util.StringUtil.triple;

import java.util.List;

import org.openrdf.model.URI;

import airldm2.core.rl.PropertyChain;
import airldm2.database.rdf.VarFactory;


public class CrawlPropertyQueryConstructor {

   private static final String TARGET_VAR = "?tar";
   private static final String PROPERTY_VAR = "?p";
   private static final String CONTEXT_PATTERN = "%context%";
   private static final String TYPE_PATTERN = "%type%";
   private static final String QUERY_HEADER = "SELECT DISTINCT " + PROPERTY_VAR + " " + CONTEXT_PATTERN + " WHERE { " + TARGET_VAR + " rdf:type " + TYPE_PATTERN + " . ";
    
   private String mContextPart;
   private URI mType;
   private PropertyChain mPropertyChain;
   private VarFactory mVarFactory;
   
   public CrawlPropertyQueryConstructor(String context, URI type, PropertyChain propChain) {
      mContextPart = makeContextPart(context);
      mType = type;
      mPropertyChain = propChain;
      mVarFactory = new VarFactory();
   }

   public String createQuery() {
      StringBuilder b = new StringBuilder();
      
      String chain = createValueChain();
      b.append(QUERY_HEADER.replace(CONTEXT_PATTERN, mContextPart)
                           .replace(TYPE_PATTERN, angleBracket(mType)));
      b.append(chain);
      b.append("} ORDER BY ").append(PROPERTY_VAR);
      return b.toString();
   }
   
   private String createValueChain() {
      StringBuilder b = new StringBuilder();
      
      if (mPropertyChain == null || mPropertyChain.isEmpty()) {
         b.append(triple(TARGET_VAR, PROPERTY_VAR, mVarFactory.next()));
         b.append("FILTER(" + PROPERTY_VAR + " != rdf:type) ");
      } else {
         List<URI> props = mPropertyChain.getList();
         b.append(triple(TARGET_VAR, angleBracket(props.get(0)), mVarFactory.next()));
         for (int i = 1; i < props.size(); i++) {
            b.append(triple(mVarFactory.current(), angleBracket(props.get(i)), mVarFactory.next()));
         }
         b.append(triple(mVarFactory.current(), PROPERTY_VAR, mVarFactory.next()));
         b.append("FILTER(" + PROPERTY_VAR + " != rdf:type) ");
      }
      
      return b.toString();
   }
   
}
