package airldm2.core.rl;

import java.io.IOException;
import java.io.Writer;

public interface ValueType {

   void write(Writer out) throws IOException;
   
}
