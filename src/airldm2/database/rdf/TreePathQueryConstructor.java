package airldm2.database.rdf;

import airldm2.classifiers.rl.tree.TreeEdge;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RbcAttribute;
import airldm2.core.rl.RbcAttributeValue;


public class TreePathQueryConstructor extends QueryConstructor {

   private static final String AGGREGATION_VAR = "?agg";
   private static final String CONTEXT_PATTERN = "%context%";
   private static final String AGGREGATION_FUNCTION_PATTERN = "%aggfun%";
   private static final String VALUE_VAR_PATTERN = "%value_var%";
   private static final String AGGREGATION_VAR_PATTERN = "%agg_var%";
   private static final String INSTANCE_VAR_PATTERN = "%instance_var%";
   private static final String INSTANCE_TYPE = "%instance_type%";
   private static final String TARGET_GRAPH = "%target_graph%";
   private static final String TARGET_FILTER = "%target_filter%";
   private static final String FEATURE_GRAPH = "%feature_graph%";
   private static final String FEATURE_FILTER = "%feature_filter%";
   private static final String EXISTS_FILTER = "%exists_filter%";
   
   private static final String QUERY_HEADER =
      "SELECT COUNT( " + INSTANCE_VAR_PATTERN + ") " + CONTEXT_PATTERN + " WHERE { "
      + INSTANCE_TYPE + " "
      + TARGET_GRAPH + " " + TARGET_FILTER + " ";
   
   private static final String QUERY_FOOTER =
      " }";
   
   
   private static final String QUERY_WITH_SIMPLE_FEATURE =
      EXISTS_FILTER + " { "
      + FEATURE_GRAPH + " " + FEATURE_FILTER
      + " } ";
   
//   private static final String QUERY_WITH_AGGREGATION_FEATURE =
//      " { "
//         + "SELECT " + INSTANCE_VAR_PATTERN + " (" + AGGREGATION_FUNCTION_PATTERN + "(" + VALUE_VAR_PATTERN + ") AS " + AGGREGATION_VAR_PATTERN + ") WHERE { "
//         + INSTANCE_TYPE + " "
//         + TARGET_GRAPH + " " + TARGET_FILTER + " "
//         + "OPTIONAL { " + FEATURE_GRAPH + "} "
//         + "} GROUP BY " + INSTANCE_VAR_PATTERN
//      + "} "
//      + FEATURE_FILTER;
      
   private TreePathQueryParameter mParam;
   
   public TreePathQueryConstructor(RDFDataDescriptor desc, String context, TreePathQueryParameter queryParam) {
      super(desc, context);
      mParam = queryParam;
   }

   public String createQuery() {
      StringBuilder b = new StringBuilder();
      b.append(QUERY_HEADER.replace(CONTEXT_PATTERN, mContextPart)
                           .replace(INSTANCE_VAR_PATTERN, mDesc.getInstanceVar())
                           .replace(INSTANCE_TYPE, createInstanceType())
                           .replace(TARGET_GRAPH, createAttributeGraph(mParam.Target))
                           .replace(TARGET_FILTER, createValueFilter(mParam.Target, mParam.TargetValueIndex))
                           );
       
      for (int i = 0; i < mParam.AncestorAttValues.size(); i++) {
         RbcAttributeValue ancAtt = mParam.AncestorAttValues.get(i);
         TreeEdge ancPath = mParam.TreePath.get(i);
         b.append(createSubQuery(i, ancAtt, ancPath.Value));
      }
      
      b.append(createSubQuery(mParam.AncestorAttValues.size(), mParam.AttValue, true));
      
      b.append(QUERY_FOOTER);
      return b.toString();
   }
   
   public String createSubQuery(int id, RbcAttributeValue ancAttValue, boolean exists) {
      RbcAttribute extendedAtt = ancAttValue.Attribute.extendGraphVariableName(id);
      String query;   
//      ValueAggregator featureAggType = ancAtt.getAggregatorType();
//      String aggVar = AGGREGATION_VAR + ancAtt.getName();
//      
//      if (ValueAggregator.isNumericOutput(featureAggType)) {
//         query = QUERY_WITH_AGGREGATION_FEATURE
//            .replace(AGGREGATION_FUNCTION_PATTERN, featureAggType.toString())
//            .replace(AGGREGATION_VAR_PATTERN, aggVar)
//            .replace(VALUE_VAR_PATTERN, ancAtt.getGraphPattern().getValueVar())
//            .replace(TARGET_GRAPH, createAttributeGraph(mParam.Target))
//            .replace(TARGET_FILTER, createValueFilter(mParam.Target, mParam.TargetValueIndex))
//            .replace(INSTANCE_TYPE, createInstanceType())
//            .replace(FEATURE_GRAPH, createAttributeGraph(ancAtt))
//            .replace(INSTANCE_VAR_PATTERN, mDesc.getInstanceVar())
//            .replace(FEATURE_FILTER, createValueFilter(ancAtt, exists, aggVar));
//      } else {
         String existsFilter = exists ? "FILTER EXISTS" : "FILTER NOT EXISTS";
      
         query = QUERY_WITH_SIMPLE_FEATURE
            .replace(EXISTS_FILTER, existsFilter)
            .replace(FEATURE_GRAPH, createAttributeGraph(extendedAtt))
            .replace(FEATURE_FILTER, createValueFilter(extendedAtt, ancAttValue.ValueKey));
//      }
     
      return query;
   }
   
   protected String createAttributeGraph(RbcAttribute att) {
      String pattern = att.getGraphPattern().getPattern();
      return pattern.replace(att.getGraphPattern().getInstanceVar(), mDesc.getInstanceVar());
   }


   private static final String ROOT_QUERY =
         "SELECT " + VALUE_VAR_PATTERN + " COUNT( DISTINCT " + INSTANCE_VAR_PATTERN + ") " + CONTEXT_PATTERN + " WHERE { "
         + INSTANCE_TYPE + " "
         + TARGET_GRAPH + " " + TARGET_FILTER + " "
         + FEATURE_GRAPH
         + " } GROUP BY " + VALUE_VAR_PATTERN;
      
   public String createRootQuery() {
      return ROOT_QUERY
            .replace(CONTEXT_PATTERN, mContextPart)
            .replace(INSTANCE_VAR_PATTERN, mDesc.getInstanceVar())
            .replace(INSTANCE_TYPE, createInstanceType())
            .replace(TARGET_GRAPH, createAttributeGraph(mParam.Target))
            .replace(TARGET_FILTER, createValueFilter(mParam.Target, mParam.TargetValueIndex))
            .replace(FEATURE_GRAPH, createAttributeGraph(mParam.AttValue.Attribute))
            .replace(VALUE_VAR_PATTERN, mParam.AttValue.Attribute.getGraphPattern().getValueVar());
   }

}
