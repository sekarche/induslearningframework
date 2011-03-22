package airldm2.core.rl;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.openrdf.model.Value;

public interface ValueType {

   int domainSize();

   String makeFilter(String varName, int valueIndex);

   int indexOf(Value value);

   List<String> getStringValues();

   void write(Writer out) throws IOException;
   
}
