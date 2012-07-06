package airldm2.core.rl;

import java.util.List;

import org.openrdf.model.Value;

public interface DiscreteType extends ValueType {

   int domainSize();

   String makeFilter(String varName, int valueIndex);

   String makeFilter(String var, String value);

   int indexOf(Value value);

   List<String> getStringValues();

   
}
