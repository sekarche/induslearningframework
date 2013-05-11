package explore.database.rdf;

import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RbcAttribute;
import airldm2.database.rdf.QueryConstructor;


public class RangeQueryConstructor extends QueryConstructor {

   private static final String CONTEXT_PATTERN = "%context%";
   private static final String VALUE_VAR_PATTERN = "%value_var%";
   private static final String INSTANCE_TYPE = "%instance_type%";
   private static final String FEATURE_GRAPH = "%feature_graph%";
   
   private static final String QUERY =
         "SELECT DISTINCT " + VALUE_VAR_PATTERN + " " + CONTEXT_PATTERN + " WHERE { "
               + INSTANCE_TYPE + " "
               + FEATURE_GRAPH
               + " } ORDER BY " + VALUE_VAR_PATTERN;
    
   private RbcAttribute mAtt;
   
   public RangeQueryConstructor(RDFDataDescriptor desc, String context, RbcAttribute att) {
      super(desc, context);
      mAtt = att;
   }

   public String createQuery() {
      String query;
      query = QUERY
            .replace(CONTEXT_PATTERN, mContextPart)
            .replace(VALUE_VAR_PATTERN, mAtt.getGraphPattern().getValueVar())
            .replace(INSTANCE_TYPE, createInstanceType())
            .replace(FEATURE_GRAPH, createAttributeGraph(mAtt));
      return query;
   }
   
}
