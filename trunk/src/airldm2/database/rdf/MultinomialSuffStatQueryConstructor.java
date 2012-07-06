package airldm2.database.rdf;

import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.ValueAggregator;


public class MultinomialSuffStatQueryConstructor extends QueryConstructor {

   private static final String AGGREGATION_VAR = "?agg";
   private static final String CONTEXT_PATTERN = "%context%";
   private static final String AGGREGATION_FUNCTION_PATTERN = "%aggfun%";
   private static final String VALUE_VAR_PATTERN = "%value_var%";
   private static final String INSTANCE_VAR_PATTERN = "%instance_var%";
   private static final String INSTANCE_TYPE = "%instance_type%";
   private static final String TARGET_GRAPH = "%target_graph%";
   private static final String TARGET_FILTER = "%target_filter%";
   private static final String FEATURE_GRAPH = "%feature_graph%";
   private static final String FEATURE_FILTER = "%feature_filter%";
   
   private static final String QUERY_WITHOUT_FEATURE =
      "SELECT COUNT(*) " + CONTEXT_PATTERN + " WHERE { "
      + INSTANCE_TYPE + " "
      + TARGET_GRAPH + " " + TARGET_FILTER
      + " }";
   
   private static final String QUERY_WITH_SIMPLE_FEATURE =
      "SELECT COUNT(*) " + CONTEXT_PATTERN + " WHERE { "
      + INSTANCE_TYPE + " "
      + TARGET_GRAPH + " " + TARGET_FILTER + " "
      + FEATURE_GRAPH + " " + FEATURE_FILTER
      + " }";
   
   private static final String QUERY_WITH_AGGREGATION_FEATURE =
      "SELECT COUNT(*) " + CONTEXT_PATTERN + " WHERE { "
      + "{ "
         + "SELECT (" + AGGREGATION_FUNCTION_PATTERN + "(" + VALUE_VAR_PATTERN + ") AS " + AGGREGATION_VAR + ") WHERE { "
         + INSTANCE_TYPE + " "
         + TARGET_GRAPH + " " + TARGET_FILTER + " "
         + "OPTIONAL { " + FEATURE_GRAPH + "} "
         + "} GROUP BY " + INSTANCE_VAR_PATTERN
      + "} "
      + FEATURE_FILTER
      + " }";
      
   private SuffStatQueryParameter mParam;
   
   public MultinomialSuffStatQueryConstructor(RDFDataDescriptor desc, String context, SuffStatQueryParameter queryParam) {
      super(desc, context);
      mParam = queryParam;
   }

   public String createQuery() {
      String query;
      if (mParam.hasFeature()) {
         ValueAggregator featureAggType = mParam.Feature.getAggregatorType();
         
         if (ValueAggregator.isNumericOutput(featureAggType)) {
            query = QUERY_WITH_AGGREGATION_FEATURE
               .replace(CONTEXT_PATTERN, mContextPart)
               .replace(AGGREGATION_FUNCTION_PATTERN, featureAggType.toString())
               .replace(VALUE_VAR_PATTERN, mParam.Feature.getGraphPattern().getValueVar())
               .replace(TARGET_GRAPH, createAttributeGraph(mParam.Target))
               .replace(TARGET_FILTER, createValueFilter(mParam.Target, mParam.TargetValueIndex))
               .replace(INSTANCE_TYPE, createInstanceType())
               .replace(FEATURE_GRAPH, createAttributeGraph(mParam.Feature))
               .replace(INSTANCE_VAR_PATTERN, mDesc.getInstanceVar())
               .replace(FEATURE_FILTER, createValueFilter(mParam.Feature, mParam.FeatureValueIndex, AGGREGATION_VAR));
         } else {
            query = QUERY_WITH_SIMPLE_FEATURE
            .replace(CONTEXT_PATTERN, mContextPart)
            .replace(INSTANCE_TYPE, createInstanceType())
            .replace(TARGET_GRAPH, createAttributeGraph(mParam.Target))
            .replace(TARGET_FILTER, createValueFilter(mParam.Target, mParam.TargetValueIndex))
            .replace(FEATURE_GRAPH, createAttributeGraph(mParam.Feature))
            .replace(FEATURE_FILTER, createValueFilter(mParam.Feature, mParam.FeatureValueIndex));
         }
      } else {
         query = QUERY_WITHOUT_FEATURE
            .replace(CONTEXT_PATTERN, mContextPart)
            .replace(INSTANCE_TYPE, createInstanceType())
            .replace(TARGET_GRAPH, createAttributeGraph(mParam.Target))
            .replace(TARGET_FILTER, createValueFilter(mParam.Target, mParam.TargetValueIndex));
      }
      
      return query;
   }

}
