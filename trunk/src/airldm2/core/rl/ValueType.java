package airldm2.core.rl;

import org.openrdf.model.Value;

public interface ValueType {

   int domainSize();

   String makeFilter(String varName, int valueIndex);

   int indexOf(Value value);
   
}
