package airldm2.database.rdf;

import static airldm2.util.Utils.angleBracket;
import static airldm2.util.Utils.triple;

import java.util.List;

import org.openrdf.model.URI;

import airldm2.core.rl.RbcAttribute;
import airldm2.core.rl.RbcAttribute.ValueAggregator;


public class SuffStatQueryConstructor {

   private static final String TARGET_VAR = "?tar";
   private static final String AGGREGATION_VAR = "?agg";
   private static final String CONTEXT_PATTERN = "%context%";
   private static final String QUERY_HEADER = "SELECT COUNT(*) FROM " + CONTEXT_PATTERN + " WHERE { ";
   private static final String QUERY_FOOTER = " }";
   private static final String AGGREGATION_FUNCTION_PATTERN = "%aggfun%";
   private static final String LAST_VAR_PATTERN = "%lastVar%";
   private static final String AGGREGATION_HEADER = "SELECT (" + AGGREGATION_FUNCTION_PATTERN + "(" + LAST_VAR_PATTERN + ") AS " + AGGREGATION_VAR + ") WHERE { ";
   
   private String mContext;
   private SuffStatQueryParameter mParam;
   private VarFactory mVarFactory;
   
   public SuffStatQueryConstructor(String mDefaultContext, SuffStatQueryParameter queryParam) {
      mContext = mDefaultContext;
      mParam = queryParam;
      mVarFactory = new VarFactory();
   }

   public String createQuery() {
      mVarFactory.reset();
      StringBuilder b = new StringBuilder();
      
      b.append(QUERY_HEADER.replace(CONTEXT_PATTERN, angleBracket(mContext)));
      
      if (mParam.hasFeature()) {
         ValueAggregator featureAggType = mParam.Feature.getAggregatorType();
         if (featureAggType == ValueAggregator.AVG ||
               featureAggType == ValueAggregator.COUNT ||
               featureAggType == ValueAggregator.MAX ||
               featureAggType == ValueAggregator.MIN) {
            StringBuilder sub = new StringBuilder();
            sub.append(createTargetType());
            sub.append(createValueChain(mParam.Target));
            sub.append(createValueFilter(mVarFactory.current(), mParam.Target, mParam.TargetValueIndex));
            
            sub.append("OPTIONAL { ");
            sub.append(createValueChain(mParam.Feature));
            sub.append("} ");
            
            b.append("{ ");
            b.append(AGGREGATION_HEADER.replace(AGGREGATION_FUNCTION_PATTERN, featureAggType.toString())
                     .replace(LAST_VAR_PATTERN, mVarFactory.current()));
            b.append(sub);
            b.append("} GROUP BY ").append(TARGET_VAR);
            b.append("} ");
            b.append(createValueFilter(AGGREGATION_VAR, mParam.Feature, mParam.FeatureValueIndex));
            
         } else {
            b.append(createTargetType());
            b.append(createValueChain(mParam.Target));
            b.append(createValueFilter(mVarFactory.current(), mParam.Target, mParam.TargetValueIndex));
            
            b.append(createValueChain(mParam.Feature));
            b.append(createValueFilter(mVarFactory.current(), mParam.Feature, mParam.FeatureValueIndex));
         }
         
      } else {
         b.append(createTargetType());
         b.append(createValueChain(mParam.Target));
         b.append(createValueFilter(mVarFactory.current(), mParam.Target, mParam.TargetValueIndex));
      }
      
      b.append(QUERY_FOOTER);
      
      return b.toString();
   }

   private String createValueChain(RbcAttribute att) {
      StringBuilder b = new StringBuilder();
      
      List<URI> props = att.getProperties();
      b.append(triple(TARGET_VAR, angleBracket(props.get(0)), mVarFactory.next()));
      for (int i = 1; i < props.size(); i++) {
         b.append(triple(mVarFactory.current(), angleBracket(props.get(i)), mVarFactory.next()));
      }

      return b.toString();
   }

   private String createValueFilter(String var, RbcAttribute att, int valueIndex) {
      StringBuilder b = new StringBuilder();
      b.append("FILTER(")
         .append(att.getValueType().makeFilter(var, valueIndex))
         .append(") ");
      return b.toString();
   }

   private String createTargetType() {
      return triple(TARGET_VAR, "rdf:type", angleBracket(mParam.TargetType));
   }
   
}
