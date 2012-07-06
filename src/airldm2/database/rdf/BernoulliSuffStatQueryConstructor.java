package airldm2.database.rdf;

import airldm2.core.rl.RDFDataDescriptor;


public class BernoulliSuffStatQueryConstructor extends QueryConstructor {

   private static final String CONTEXT_PATTERN = "%context%";
   private static final String INSTANCE_TYPE = "%instance_type%";
   private static final String INSTANCE_VAR_PATTERN = "%instance_var%";
   private static final String TARGET_GRAPH = "%target_graph%";
   private static final String TARGET_FILTER = "%target_filter%";
   private static final String FEATURE_GRAPH = "%feature_graph%";
   private static final String FEATURE_FILTER = "%feature_filter%";
   
   private static final String QUERY =
      "SELECT COUNT(DISTINCT " + INSTANCE_VAR_PATTERN + ") " + CONTEXT_PATTERN + " WHERE { "
      + INSTANCE_TYPE + " "
      + TARGET_GRAPH + " " + TARGET_FILTER + " "
      + FEATURE_GRAPH + " " + FEATURE_FILTER
      + " }";
   
   private SuffStatQueryParameter mParam;
   
   public BernoulliSuffStatQueryConstructor(RDFDataDescriptor desc, String context, SuffStatQueryParameter queryParam) {
      super(desc, context);
      mParam = queryParam;
   }

   public String createQuery() {
      String query;
      
      query = QUERY
      .replace(CONTEXT_PATTERN, mContextPart)
      .replace(INSTANCE_TYPE, createInstanceType())
      .replace(INSTANCE_VAR_PATTERN, mDesc.getInstanceVar())
      .replace(TARGET_GRAPH, createAttributeGraph(mParam.Target))
      .replace(TARGET_FILTER, createValueFilter(mParam.Target, mParam.TargetValueIndex))
      .replace(FEATURE_GRAPH, createAttributeGraph(mParam.Feature))
      .replace(FEATURE_FILTER, createValueFilter(mParam.Feature, mParam.FeatureValueIndex));
      
      return query;
   }

}
