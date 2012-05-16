package airldm2.database.rdf;

import static airldm2.util.StringUtil.angleBracket;

import org.openrdf.model.URI;

import airldm2.core.rl.RDFDataDescriptor;


public class InstanceQueryConstructor extends QueryConstructor {

   private static final String CONTEXT_PATTERN = "%context%";
   private static final String TYPE_PATTERN = "%type%";
   
   private static final String QUERY =
      "SELECT ?x " + CONTEXT_PATTERN + " WHERE { ?x rdf:type " + TYPE_PATTERN + " . }";
   
   private URI mType;
   
   public InstanceQueryConstructor(RDFDataDescriptor desc, String context, URI type) {
      super(desc, context);
      mType = type;
   }

   public String createQuery() {
      return QUERY.replace(CONTEXT_PATTERN, mContextPart)
                  .replace(TYPE_PATTERN, angleBracket(mType));
   }
   
}
