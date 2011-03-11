package airldm2.database.rdf;

import static airldm2.util.Utils.angleBracket;

import org.openrdf.model.URI;


public class InstanceQueryConstructor {

   private static final String CONTEXT_PATTERN = "%context%";
   private static final String TYPE_PATTERN = "%type%";
   private static final String QUERY = "SELECT ?x from " + CONTEXT_PATTERN + " WHERE { ?x rdf:type " + TYPE_PATTERN + " . }";
   
   private String mContext;
   private URI mType;
   
   public InstanceQueryConstructor(String context, URI type) {
      mContext = context;
      mType = type;
   }

   public String createQuery() {
      return QUERY.replace(CONTEXT_PATTERN, angleBracket(mContext))
                  .replace(TYPE_PATTERN, angleBracket(mType));
   }
   
}
