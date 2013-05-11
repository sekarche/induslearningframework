package airldm2.database.rdf;

import airldm2.core.rl.RDFDataDescriptor;


public class MultinomialSuffStatForAllHierarchyQueryConstructor extends QueryConstructor {

   private static final String CONTEXT_PATTERN = "%context%";
   private static final String INSTANCE_TYPE = "%instance_type%";
   private static final String TARGET_GRAPH = "%target_graph%";
   private static final String TARGET_FILTER = "%target_filter%";
   private static final String FEATURE_GRAPH = "%feature_graph%";
   
   private static final String HIERARCHY_VAR_PATTERN = "%hierarchy_var%";
   
   private static final String QUERY_WITH_SIMPLE_FEATURE =
      "SELECT " + HIERARCHY_VAR_PATTERN + " COUNT(" + HIERARCHY_VAR_PATTERN + ") " + CONTEXT_PATTERN + " WHERE { "
      + INSTANCE_TYPE + " "
      + TARGET_GRAPH + " " + TARGET_FILTER + " "
      + FEATURE_GRAPH
      + " } GROUP BY " + HIERARCHY_VAR_PATTERN;
   
   private SuffStatQueryParameter mParam;
   
   public MultinomialSuffStatForAllHierarchyQueryConstructor(RDFDataDescriptor desc, String context, SuffStatQueryParameter queryParam) {
      super(desc, context);
      mParam = queryParam;
   }

   public String createQuery() {
      String query;
      query = QUERY_WITH_SIMPLE_FEATURE
         .replace(CONTEXT_PATTERN, mContextPart)
         .replace(INSTANCE_TYPE, createInstanceType())
         .replace(TARGET_GRAPH, createAttributeGraph(mParam.Target))
         .replace(TARGET_FILTER, createValueFilter(mParam.Target, mParam.TargetValueIndex))
         .replace(FEATURE_GRAPH, createAttributeGraph(mParam.Feature));
      
      query = query.replace(HIERARCHY_VAR_PATTERN, mParam.Feature.getGraphPattern().getHierarchyVar());
      
      return query;
   }

}
