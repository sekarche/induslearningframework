package airldm2.database.rdf;

import static airldm2.util.StringUtil.angleBracket;

import org.openrdf.model.URI;

import airldm2.core.rl.RDFDataDescriptor;


public class InstanceQueryConstructor extends QueryConstructor {

   private static final String CONTEXT_PATTERN = "%context%";
   private static final String TYPE_PATTERN = "%type%";
   private static final String INSTANCE_TYPE = "%instance_type%";
   private static final String TARGET_GRAPH = "%target_graph%";
   private static final String TARGET_FILTER = "%target_filter%";
   
   private static final String ALL_QUERY =
      "SELECT ?x " + CONTEXT_PATTERN + " WHERE { ?x rdf:type " + TYPE_PATTERN + " . } ";
   
   private static final String ONE_CLASS_QUERY =
      "SELECT ?x " + CONTEXT_PATTERN + " WHERE { "
         + INSTANCE_TYPE + " "
         + TARGET_GRAPH + " " + TARGET_FILTER
         + " }";
   
   private URI mType;
   private SuffStatQueryParameter mParam;
   
   public InstanceQueryConstructor(RDFDataDescriptor desc, String context, URI type) {
      super(desc, context);
      mType = type;
   }

   public InstanceQueryConstructor(RDFDataDescriptor desc, String context, SuffStatQueryParameter queryParam) {
      super(desc, context);
      mParam = queryParam;
   }

   public String createQuery() {
      if (mType != null) {
         return ALL_QUERY.replace(CONTEXT_PATTERN, mContextPart)
                     .replace(TYPE_PATTERN, angleBracket(mType));
      } else {
         return ONE_CLASS_QUERY.replace(CONTEXT_PATTERN, mContextPart)
                     .replace(INSTANCE_TYPE, createInstanceType())
                     .replace(TARGET_GRAPH, createAttributeGraph(mParam.Target))
                     .replace(TARGET_FILTER, createValueFilter(mParam.Target, mParam.TargetValueIndex));
      }
   }
   
}
