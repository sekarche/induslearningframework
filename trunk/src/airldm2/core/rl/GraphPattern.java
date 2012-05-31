package airldm2.core.rl;

import org.openrdf.model.URI;

import airldm2.database.rdf.RDFDatabaseConnectionFactory;

import static airldm2.util.StringUtil.triple;

public class GraphPattern {

   private String HIERARCHY_PATTERN = "<HIERARCHY>";
   
   private String mInstanceVar;
   private String mValueVar;
   private String mHierarchyVar;
   private String mPattern;
   private String mHierarchyPattern;

   private URI mExtendedHierarchy;
   
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
      if (mHierarchyPattern == null) {
         return mPattern.replace(HIERARCHY_PATTERN, "");
      } else {
         return mPattern.replace(HIERARCHY_PATTERN, mHierarchyPattern);
      }
   }

   public URI getExtendedHierarchy() {
      return mExtendedHierarchy;
   }
   
   public GraphPattern extendWithHierarchy(URI node, boolean isLeaf) {
      GraphPattern extended = new GraphPattern(mInstanceVar, mValueVar, mHierarchyVar, mPattern);
      extended.mHierarchyPattern = " FILTER(" + getHierarchyVar() + " = <" + node + ">) ";
      extended.mExtendedHierarchy = node;
      
      if (RDFDatabaseConnectionFactory.QUERY_INFERENCE && !isLeaf) {
         String transVar = "?transitive";
         extended.mHierarchyPattern = 
            "{ SELECT * WHERE { "
            + triple(getHierarchyVar(), "rdfs:subClassOf", transVar)
            + " } } "
            + "OPTION(TRANSITIVE, t_distinct, t_in(" + getHierarchyVar() + "), t_out(" + transVar + ")) . "
            + "FILTER(" + transVar + " = <" + node + ">) ";
      }
      
      return extended;
   }
   
   @Override
   public String toString() {
      return mPattern + " " + mHierarchyPattern;
   }
   
}
