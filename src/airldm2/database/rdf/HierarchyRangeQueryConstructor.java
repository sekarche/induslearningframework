package airldm2.database.rdf;

import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RbcAttribute;


public class HierarchyRangeQueryConstructor extends QueryConstructor {

   private static final String CONTEXT_PATTERN = "%context%";
   private static final String VALUE_VAR_PATTERN = "%value_var%";
   private static final String INSTANCE_TYPE = "%instance_type%";
   private static final String TARGET_GRAPH = "%target_graph%";
   private static final String FEATURE_GRAPH = "%feature_graph%";
   
   private static final String QUERY =
         "SELECT DISTINCT " + VALUE_VAR_PATTERN + " " + CONTEXT_PATTERN + " WHERE { "
         + INSTANCE_TYPE + " "
         + TARGET_GRAPH + " "
         + FEATURE_GRAPH
         + " }";
   
   private final RbcAttribute mAtt;
   
   public HierarchyRangeQueryConstructor(RDFDataDescriptor desc, String context, RbcAttribute attribute) {
      super(desc, context);
      mAtt = attribute;
   }

   public String createQuery() {
      return QUERY
            .replace(CONTEXT_PATTERN, mContextPart)
            .replace(VALUE_VAR_PATTERN, mAtt.getGraphPattern().getValueVar())
            .replace(INSTANCE_TYPE, createInstanceType())
            .replace(TARGET_GRAPH, createAttributeGraph(mDesc.getTargetAttribute()))
            .replace(FEATURE_GRAPH, createAttributeGraph(mAtt));
   }
   
}
