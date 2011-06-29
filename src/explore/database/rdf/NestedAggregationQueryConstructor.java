package explore.database.rdf;

import static airldm2.util.StringUtil.angleBracket;
import static airldm2.util.StringUtil.makeContextPart;

import org.openrdf.model.URI;

import airldm2.core.rl.PropertyChain;
import airldm2.database.rdf.QueryUtil;
import airldm2.database.rdf.VarFactory;


public class NestedAggregationQueryConstructor {

   public enum Aggregator { COUNT, MIN, MAX, AVG, SUM }
   
   private static final String TARGET_VAR = "?tar";
   private static final String LAST_VAR_PATTERN = "%lastVar%";
   private static final String CONTEXT_PATTERN = "%context%";
   private static final String TYPE_PATTERN = "%type%";
   private static final String AGG_OUTER_PATTERN = "%agg_out%";
   private static final String AGG_INNER_PATTERN = "%agg_in%";
   private static final String QUERY_HEADER = "SELECT " + AGG_OUTER_PATTERN + "(?agg) WHERE { { SELECT " + AGG_INNER_PATTERN + "(" + LAST_VAR_PATTERN + ") AS ?agg " + CONTEXT_PATTERN + " WHERE { " + TARGET_VAR + " rdf:type " + TYPE_PATTERN + " . ";
    
   private String mContextPart;
   private URI mType;
   private PropertyChain mPropChain;
   private VarFactory mVarFactory;
   private Aggregator mAggOuter;
   private Aggregator mAggInner;
   private boolean mNumericFilter;
   
   public NestedAggregationQueryConstructor(String context, URI type, PropertyChain propChain, Aggregator outer, Aggregator inner, boolean numericFilter) {
      mContextPart = makeContextPart(context);
      mType = type;
      mPropChain = propChain;
      mAggOuter = outer;
      mAggInner = inner;
      mNumericFilter = numericFilter;
      mVarFactory = new VarFactory();
   }

   public String createQuery() {
      StringBuilder b = new StringBuilder();
      
      String chain = QueryUtil.createValueChain(mPropChain, TARGET_VAR, mVarFactory);
      b.append(QUERY_HEADER.replace(LAST_VAR_PATTERN, mVarFactory.current())
                           .replace(CONTEXT_PATTERN, mContextPart)
                           .replace(TYPE_PATTERN, angleBracket(mType))
                           .replace(AGG_OUTER_PATTERN, mAggOuter.toString())
                           .replace(AGG_INNER_PATTERN, mAggInner.toString()));
      b.append(chain);
      if (mNumericFilter) {
         b.append("FILTER (").append(mVarFactory.current()).append(" > " + RangeTypeQueryConstructor.MIN_NUMER + ") ");
      }
      b.append("} GROUP BY ").append(TARGET_VAR).append(" } }");
      return b.toString();
   }
      
}
