package airldm2.core.rl;

import org.openrdf.model.URI;

import airldm2.database.rdf.RDFDatabaseConnectionFactory;

import static airldm2.util.StringUtil.triple;

public class GraphPattern {

   private String mInstanceVar;
   private String mValueVar;
   private String mHierarchyVar;
   private String mPattern;
   
   public GraphPattern(String instanceVar, String valueVar, String hierarchyVar, String pattern) {
      mInstanceVar = instanceVar;
      mValueVar = valueVar;
      mHierarchyVar = hierarchyVar;
      mPattern = pattern;
   }

   public String getInstanceVar() {
      return "?" + mInstanceVar;
   }
   
   public String getValueVar() {
      return "?" + mValueVar;
   }
   
   public String getHierarchyVar() {
      return "?" + mHierarchyVar;
   }
   
   public String getPattern() {
      return mPattern;
   }
   
   public GraphPattern extendWithHierarchy(URI node, boolean isLeaf) {
      String filter = " FILTER(" + getHierarchyVar() + " = <" + node + ">) ";
      
      if (RDFDatabaseConnectionFactory.QUERY_INFERENCE && !isLeaf) {
         String transVar = "?transitive";
         filter = 
            "{ SELECT * WHERE { "
            + triple(getHierarchyVar(), "rdfs:subClassOf", transVar)
            + " } } "
            + "OPTION(TRANSITIVE, t_distinct, t_in(" + getHierarchyVar() + "), t_out(" + transVar + ")) . "
            + "FILTER(" + transVar + " = <" + node + ">) ";
      }
      
      return new GraphPattern(mInstanceVar, mValueVar, mHierarchyVar, mPattern + filter);
   }
   
   @Override
   public String toString() {
      return mPattern;
   }
   
}
