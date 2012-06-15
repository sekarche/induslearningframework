package explore.database.rdf;

import static airldm2.util.StringUtil.makeContextPart;


public class SubclassQueryConstructor {

   private static final String CONTEXT_PATTERN = "%context%";
   private static final String QUERY =
      "SELECT ?x1 ?x2 " + CONTEXT_PATTERN + " WHERE { ?x1 <http://www.w3.org/TR/rdf-schema/subClassOf> ?x2 . }";
    
   private String mContextPart;
   
   public SubclassQueryConstructor(String context) {
      mContextPart = makeContextPart(context);
   }

   public String createQuery() {
      return QUERY.replace(CONTEXT_PATTERN, mContextPart);
   }
   
}
