package airldm2.database.rdf;

public class VarFactory {
   
   private static final String ANON_VAR = "?v";
   
   private int mNext;
   
   public void reset() {
      mNext = 0;
   }
   
   public String current() {
      return ANON_VAR + mNext;
   }
   
   public String next() {
      mNext++;
      return current();
   }
   
}