package explore.database.rdf;

import static airldm2.util.StringUtil.angleBracket;
import static airldm2.util.StringUtil.makeContextPart;

import org.openrdf.model.URI;

import airldm2.core.rl.PropertyChain;
import airldm2.database.rdf.QueryUtil;
import airldm2.database.rdf.VarFactory;


public class RangeTypeQueryConstructor {

   public enum RangeType { IRI, NUMERIC, STRING }
   
   private static final String TARGET_VAR = "?tar";
   private static final String LAST_VAR_PATTERN = "%lastVar%";
   private static final String CONTEXT_PATTERN = "%context%";
   private static final String TYPE_PATTERN = "%type%";
   private static final String QUERY_HEADER = "SELECT COUNT(" + LAST_VAR_PATTERN + ") " + CONTEXT_PATTERN + " WHERE { " + TARGET_VAR + " rdf:type " + TYPE_PATTERN + " . ";
    
   private String mContextPart;
   private URI mType;
   private PropertyChain mPropChain;
   private VarFactory mVarFactory;
   private RangeType mRangeType;
   
   public RangeTypeQueryConstructor(String context, URI type, PropertyChain propChain, RangeType rangeType) {
      mContextPart = makeContextPart(context);
      mType = type;
      mPropChain = propChain;
      mRangeType = rangeType;
      mVarFactory = new VarFactory();
   }

   public String createQuery() {
      StringBuilder b = new StringBuilder();
      
      String chain = QueryUtil.createValueChain(mPropChain, TARGET_VAR, mVarFactory);
      String filter = createValueFilter(mVarFactory.current());
      b.append(QUERY_HEADER.replace(LAST_VAR_PATTERN, mVarFactory.current())
                           .replace(CONTEXT_PATTERN, mContextPart)
                           .replace(TYPE_PATTERN, angleBracket(mType)));
      b.append(chain);
      b.append(filter);
      b.append("}");
      return b.toString();
   }

   private String createValueFilter(String var) {
      StringBuilder b = new StringBuilder();
      if (mRangeType == RangeType.IRI) {
         b.append("FILTER isIRI(").append(var).append(") ");
      } else if (mRangeType == RangeType.NUMERIC) {
         b.append("FILTER(").append(var).append(" > -1000000) ");
      } else if (mRangeType == RangeType.STRING) {
         b.append("FILTER isLiteral(").append(var).append(") ");
      }
      
      return b.toString();
   }
   
}
