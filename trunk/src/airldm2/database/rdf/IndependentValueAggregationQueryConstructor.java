package airldm2.database.rdf;

import static airldm2.util.StringUtil.angleBracket;
import static airldm2.util.StringUtil.makeContextPart;
import static airldm2.util.StringUtil.triple;

import java.util.List;

import org.openrdf.model.URI;

import airldm2.core.rl.RbcAttribute;


public class IndependentValueAggregationQueryConstructor {

   private static final String CONTEXT_PATTERN = "%context%";
   private static final String LAST_VAR_PATTERN = "%lastVar%";
   private static final String AGGREGATION_HEADER = "SELECT COUNT(" + LAST_VAR_PATTERN + ") " + CONTEXT_PATTERN + " WHERE { ";
   
   private String mContextPart;
   private URI mInstance;
   private RbcAttribute mAttribute;
   private VarFactory mVarFactory;
   private int mValueIndex;
   
   public IndependentValueAggregationQueryConstructor(String context, URI instance, RbcAttribute attribute, int valueIndex) {
      mContextPart = makeContextPart(context);
      mInstance = instance;
      mAttribute = attribute;
      mValueIndex = valueIndex;
      mVarFactory = new VarFactory();
   }

   public String createQuery() {
      mVarFactory.reset();
      StringBuilder b = new StringBuilder();
      
      String chain = createValueChain(mAttribute);
      String header = AGGREGATION_HEADER.replace(CONTEXT_PATTERN, mContextPart)
                                       .replace(LAST_VAR_PATTERN, mVarFactory.current()); 
      b.append(header)
       .append(chain)
       .append("}");
      
      return b.toString();
   }
   
   private String createValueChain(RbcAttribute att) {
      StringBuilder b = new StringBuilder();
      
      List<URI> props = att.getProperties().getList();
      b.append(triple(angleBracket(mInstance), angleBracket(props.get(0)), mVarFactory.next()));
      for (int i = 1; i < props.size(); i++) {
         b.append(triple(mVarFactory.current(), angleBracket(props.get(i)), mVarFactory.next()));
      }

      b.append(createValueFilter(mVarFactory.current(), mAttribute, mValueIndex));
      
      return b.toString();
   }
   
   private String createValueFilter(String var, RbcAttribute att, int valueIndex) {
      StringBuilder b = new StringBuilder();
      b.append("FILTER(")
         .append(att.getValueType().makeFilter(var, valueIndex))
         .append(") ");
      return b.toString();
   }
   
}
