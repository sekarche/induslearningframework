package airldm2.core.rl;

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
      return mInstanceVar;
   }
   
   public String getValueVar() {
      return mValueVar;
   }
   
   public String getHierarchyVar() {
      return mHierarchyVar;
   }
   
   public String getPattern() {
      return mPattern;
   }
   
   @Override
   public String toString() {
      return mPattern;
   }
   
}
