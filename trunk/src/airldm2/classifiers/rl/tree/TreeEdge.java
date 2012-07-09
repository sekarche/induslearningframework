package airldm2.classifiers.rl.tree;

public class TreeEdge {
   
   public boolean Value;
   
   public TreeEdge(boolean v) {
      Value = v;
   }
   
   @Override
   public String toString() {
      return String.valueOf(Value);
   }
   
}
