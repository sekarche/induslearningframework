package airldm2.database.rdf;

import org.openrdf.model.URI;

import airldm2.util.Utils;


public class InstanceQueryConstructor {

   private static final String CONTEXT_PATTERN = "%context%";
   private static final String TYPE_PATTERN = "%type%";
   private static final String QUERY = "SELECT ?x from " + CONTEXT_PATTERN + " WHERE { ?x rdf:type " + TYPE_PATTERN + " . }";
   
   private String mContext;
   private URI mType;
   
   public InstanceQueryConstructor(String mDefaultContext, URI type) {
      mContext = mDefaultContext;
      mType = type;
   }

   public String createQuery() {
      return QUERY.replace(CONTEXT_PATTERN, Utils.angleBracket(mContext))
                  .replace(TYPE_PATTERN, Utils.angleBracket(mType));
   }
   
}
