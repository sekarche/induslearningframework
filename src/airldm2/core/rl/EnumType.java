package airldm2.core.rl;

import java.util.List;

import org.openrdf.model.URI;

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
   public String makeFilter(String varName, int valueIndex) {
      return new StringBuilder()
         .append(varName)
         .append(" = <")
         .append(mDomain.get(valueIndex))
         .append(">")
         .toString();
   }

}
