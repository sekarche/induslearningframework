package airldm2.core.rl;

public interface ValueType {

   int domainSize();

   String makeFilter(String varName, int valueIndex);
   
}
