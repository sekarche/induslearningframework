package airldm2.database.rdf;

import static airldm2.util.Utils.angleBracket;
import static airldm2.util.Utils.triple;

import java.util.List;

import org.openrdf.model.URI;

import airldm2.core.rl.RbcAttribute;


public class AggregationQueryConstructor {

   private static final String CONTEXT_PATTERN = "%context%";
   private static final String AGGREGATION_FUNCTION_PATTERN = "%aggfun%";
   private static final String LAST_VAR_PATTERN = "%lastVar%";
   private static final String AGGREGATION_HEADER = "SELECT " + AGGREGATION_FUNCTION_PATTERN + "(" + LAST_VAR_PATTERN + ") FROM " + CONTEXT_PATTERN + " WHERE { ";
   
   private String mContext;
   private URI mInstance;
   private RbcAttribute mAttribute;
   private VarFactory mVarFactory;
      
   public AggregationQueryConstructor(String context, URI instance, RbcAttribute attribute) {
      mContext = context;
      mInstance = instance;
      mAttribute = attribute;
      mVarFactory = new VarFactory();
   }

   public String createQuery() {
      mVarFactory.reset();
      StringBuilder b = new StringBuilder();
      
      String chain = createValueChain(mAttribute);
      String header = AGGREGATION_HEADER.replace(CONTEXT_PATTERN, angleBracket(mContext))
                                       .replace(AGGREGATION_FUNCTION_PATTERN, mAttribute.getAggregatorType().toString())
                                       .replace(LAST_VAR_PATTERN, mVarFactory.current()); 
      b.append(header)
       .append(chain)
       .append("} GROUP BY ").append(angleBracket(mInstance));
      
      return b.toString();
   }
   
   private String createValueChain(RbcAttribute att) {
      StringBuilder b = new StringBuilder();
      
      List<URI> props = att.getProperties();
      b.append(triple(angleBracket(mInstance), angleBracket(props.get(0)), mVarFactory.next()));
      for (int i = 1; i < props.size(); i++) {
         b.append(triple(mVarFactory.current(), angleBracket(props.get(i)), mVarFactory.next()));
      }

      return b.toString();
   }
   
}
