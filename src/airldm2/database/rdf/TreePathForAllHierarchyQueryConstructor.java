package airldm2.database.rdf;

import static airldm2.util.StringUtil.angleBracket;
import static airldm2.util.StringUtil.triple;
import airldm2.classifiers.rl.estimator.Category;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RbcAttribute;
import airldm2.core.rl.ValueAggregator;


public class TreePathForAllHierarchyQueryConstructor extends QueryConstructor {

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

   private static final String HIERARCHY_VAR_PATTERN = "%hierarchy_var%";
   
   private static final String QUERY_HEADER =
      "SELECT COUNT(" + INSTANCE_VAR_PATTERN + ") " + CONTEXT_PATTERN + " WHERE { ";
   
   private static final String QUERY_FOOTER =
      " }";
   
   
   private static final String QUERY_WITH_SIMPLE_FEATURE =
      " "
      + INSTANCE_TYPE + " "
      + TARGET_GRAPH + " " + TARGET_FILTER + " "
      + FEATURE_GRAPH + " " + FEATURE_FILTER
      + " ";
   
   private static final String QUERY_WITH_AGGREGATION_FEATURE =
      " { "
         + "SELECT " + INSTANCE_VAR_PATTERN + " (" + AGGREGATION_FUNCTION_PATTERN + "(" + VALUE_VAR_PATTERN + ") AS " + AGGREGATION_VAR_PATTERN + ") WHERE { "
         + INSTANCE_TYPE + " "
         + TARGET_GRAPH + " " + TARGET_FILTER + " "
         + "OPTIONAL { " + FEATURE_GRAPH + "} "
         + "} GROUP BY " + INSTANCE_VAR_PATTERN
      + "} "
      + FEATURE_FILTER;
      
   private TreePathQueryParameter mParam;
   
   public TreePathForAllHierarchyQueryConstructor(RDFDataDescriptor desc, String context, TreePathQueryParameter queryParam) {
      super(desc, context);
      mParam = queryParam;
   }

   public String createQuery() {
      StringBuilder b = new StringBuilder();
      b.append(QUERY_HEADER.replace(CONTEXT_PATTERN, mContextPart)
                           .replace(INSTANCE_VAR_PATTERN, mDesc.getInstanceVar()));
       
      for (int i = 0; i < mParam.AncestorAtts.size(); i++) {
         RbcAttribute ancAtt = mParam.AncestorAtts.get(i);
         Category ancCategory = mParam.TreePath.get(i);
         b.append(createSubQuery(ancAtt, ancCategory.getIndex()));
      }
      
      b.append(createSubQuery(mParam.Feature, mParam.FeatureValueIndex));
      
      b.append(QUERY_FOOTER);
      return b.toString();
   }
   
   public String createSubQuery(RbcAttribute att, int valueIndex) {
      String query;   
      ValueAggregator featureAggType = att.getAggregatorType();
      String aggVar = AGGREGATION_VAR + att.getName();
      
      if (ValueAggregator.isNumericOutput(featureAggType)) {
         query = QUERY_WITH_AGGREGATION_FEATURE
            .replace(AGGREGATION_FUNCTION_PATTERN, featureAggType.toString())
            .replace(AGGREGATION_VAR_PATTERN, aggVar)
            .replace(VALUE_VAR_PATTERN, att.getGraphPattern().getValueVar())
            .replace(TARGET_GRAPH, createAttributeGraph(mParam.Target))
            .replace(TARGET_FILTER, createValueFilter(mParam.Target, mParam.TargetValueIndex))
            .replace(INSTANCE_TYPE, createInstanceType())
            .replace(FEATURE_GRAPH, createAttributeGraph(att))
            .replace(INSTANCE_VAR_PATTERN, mDesc.getInstanceVar())
            .replace(FEATURE_FILTER, createValueFilter(att, valueIndex, aggVar));
      } else {
         query = QUERY_WITH_SIMPLE_FEATURE
            .replace(INSTANCE_TYPE, createInstanceType())
            .replace(TARGET_GRAPH, createAttributeGraph(mParam.Target))
            .replace(TARGET_FILTER, createValueFilter(mParam.Target, mParam.TargetValueIndex))
            .replace(FEATURE_GRAPH, createAttributeGraph(att))
            .replace(FEATURE_FILTER, createValueFilter(att, valueIndex));
      }
     
      return query;
   }

   private String createInstanceType() {
      return triple(mDesc.getInstanceVar(), "a", angleBracket(mParam.TargetType));
   }
   
}
