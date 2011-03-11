package airldm2.core.rl;

import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.Value;

public class EnumType implements ValueType {

   private List<URI> mDomain;
   
   public EnumType(List<URI> domain) {
      mDomain = domain;
   }

   @Override
   public int domainSize() {
      return mDomain.size();
   }

   @Override
   public int indexOf(Value value) {
      if (value instanceof URI) {
         URI uri = (URI) value;
         return mDomain.indexOf(uri);
      }
      
      return -1;
   }

   @Override
   public String makeFilter(String varName, int valueIndex) {
      return new StringBuilder()
         .append(varName)
         .append(" = <")
         .append(mDomain.get(valueIndex))
         .append(">")
         .toString();
   }

}
