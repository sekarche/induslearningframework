package airldm2.database.rdf;

import static airldm2.util.Utils.angleBracket;
import static airldm2.util.Utils.makeContextPart;

import org.openrdf.model.URI;


public class InstanceQueryConstructor {

   private static final String CONTEXT_PATTERN = "%context%";
   private static final String TYPE_PATTERN = "%type%";
   private static final String QUERY = "SELECT ?x from " + CONTEXT_PATTERN + " WHERE { ?x rdf:type " + TYPE_PATTERN + " . }";
   
   private String mContextPart;
   private URI mType;
   
   public InstanceQueryConstructor(String context, URI type) {
      mContextPart = makeContextPart(context);
      mType = type;
   }

   public String createQuery() {
      return QUERY.replace(CONTEXT_PATTERN, mContextPart)
                  .replace(TYPE_PATTERN, angleBracket(mType));
   }
   
}
