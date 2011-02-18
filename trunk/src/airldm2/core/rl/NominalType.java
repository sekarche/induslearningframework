package airldm2.core.rl;

import java.util.List;

public class NominalType implements ValueType {

   private List<String> mDomain;
   
   public NominalType(List<String> domain) {
      mDomain = domain;
   }

   @Override
   public int domainSize() {
      return mDomain.size();
   }

}
