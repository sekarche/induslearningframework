package airldm2.database.rdf;

import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.ValueAggregator;


public class SumSuffStatQueryConstructor extends QueryConstructor {

   private static final String AGGREGATION_VAR = "?agg";
   private static final String CONTEXT_PATTERN = "%context%";
   private static final String AGGREGATION_FUNCTION_PATTERN = "%aggfun%";
   private static final String VALUE_VAR_PATTERN = "%value_var%";
   private static final String INSTANCE_VAR_PATTERN = "%instance_var%";
   private static final String INSTANCE_TYPE = "%instance_type%";
   private static final String TARGET_GRAPH = "%target_graph%";
   private static final String TARGET_FILTER = "%target_filter%";
   private static final String FEATURE_GRAPH = "%feature_graph%";
   
   private static final String QUERY_WITH_SIMPLE_FEATURE =
      "SELECT SUM(" + VALUE_VAR_PATTERN + ") " + CONTEXT_PATTERN + " WHERE { "
      + INSTANCE_TYPE + " "
      + TARGET_GRAPH + " " + TARGET_FILTER + " "
      + FEATURE_GRAPH
      + " }";
   
   private static final String QUERY_WITH_AGGREGATION_FEATURE =
      "SELECT SUM(" + AGGREGATION_VAR + ") " + CONTEXT_PATTERN + " WHERE { "
      + "{ "
         + "SELECT " + INSTANCE_VAR_PATTERN + " (" + AGGREGATION_FUNCTION_PATTERN + "(" + VALUE_VAR_PATTERN + ") AS " + AGGREGATION_VAR + ") WHERE { "
         + INSTANCE_TYPE + " "
         + TARGET_GRAPH + " " + TARGET_FILTER + " "
         + FEATURE_GRAPH
         + "} GROUP BY " + INSTANCE_VAR_PATTERN
      + "} "
      + " }";
      
   private SuffStatQueryParameter mParam;
   
   public SumSuffStatQueryConstructor(RDFDataDescriptor desc, String context, SuffStatQueryParameter queryParam) {
      super(desc, context);
      mParam = queryParam;
   }

   public String createQuery() {
      String query;
      ValueAggregator featureAggType = mParam.Feature.getAggregatorType();
      
      if (featureAggType == ValueAggregator.NONE) {
         query = QUERY_WITH_SIMPLE_FEATURE
            .replace(CONTEXT_PATTERN, mContextPart)
            .replace(VALUE_VAR_PATTERN, mParam.Feature.getGraphPattern().getValueVar())
            .replace(INSTANCE_TYPE, createInstanceType())
            .replace(TARGET_GRAPH, createAttributeGraph(mParam.Target))
            .replace(TARGET_FILTER, createValueFilter(mParam.Target, mParam.TargetValueIndex))
            .replace(FEATURE_GRAPH, createAttributeGraph(mParam.Feature));
      } else {
         query = QUERY_WITH_AGGREGATION_FEATURE
            .replace(CONTEXT_PATTERN, mContextPart)
            .replace(AGGREGATION_FUNCTION_PATTERN, featureAggType.toString())
            .replace(VALUE_VAR_PATTERN, mParam.Feature.getGraphPattern().getValueVar())
            .replace(TARGET_GRAPH, createAttributeGraph(mParam.Target))
            .replace(TARGET_FILTER, createValueFilter(mParam.Target, mParam.TargetValueIndex))
            .replace(INSTANCE_TYPE, createInstanceType())
            .replace(FEATURE_GRAPH, createAttributeGraph(mParam.Feature))
            .replace(INSTANCE_VAR_PATTERN, mDesc.getInstanceVar());
      }
      
      return query;
   }

}
