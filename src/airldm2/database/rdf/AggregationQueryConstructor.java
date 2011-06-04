package airldm2.database.rdf;

import static airldm2.util.StringUtil.angleBracket;
import static airldm2.util.StringUtil.makeContextPart;

import org.openrdf.model.URI;

import airldm2.core.rl.RbcAttribute;

public class AggregationQueryConstructor {

   private static final String CONTEXT_PATTERN = "%context%";
   private static final String AGGREGATION_FUNCTION_PATTERN = "%aggfun%";
   private static final String LAST_VAR_PATTERN = "%lastVar%";
   private static final String AGGREGATION_HEADER = "SELECT " + AGGREGATION_FUNCTION_PATTERN + "(" + LAST_VAR_PATTERN + ") " + CONTEXT_PATTERN + " WHERE { ";

   private String mContextPart;
   private URI mInstance;
   private RbcAttribute mAttribute;
   private VarFactory mVarFactory;

   public AggregationQueryConstructor(String context, URI instance, RbcAttribute attribute) {
      mContextPart = makeContextPart(context);
      mInstance = instance;
      mAttribute = attribute;
      mVarFactory = new VarFactory();
   }

   public String createQuery() {
      mVarFactory.reset();
      StringBuilder b = new StringBuilder();

      String chain = QueryUtil.createValueChain(mAttribute.getProperties(), angleBracket(mInstance), mVarFactory);
      String header = AGGREGATION_HEADER.replace(CONTEXT_PATTERN, mContextPart)
      .replace(AGGREGATION_FUNCTION_PATTERN, mAttribute.getAggregatorType().toString())
      .replace(LAST_VAR_PATTERN, mVarFactory.current()); 
      b.append(header)
      .append(chain)
      .append("}");

      return b.toString();
   }

}
