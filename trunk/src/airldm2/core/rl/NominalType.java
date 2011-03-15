package airldm2.core.rl;

import java.util.List;

import org.openrdf.model.Value;

public class NominalType implements ValueType {

   private List<String> mDomain;
   
   public NominalType(List<String> domain) {
      mDomain = domain;
   }

   @Override
   public int domainSize() {
      return mDomain.size();
   }

   @Override
   public int indexOf(Value value) {
      return mDomain.indexOf(value.stringValue());
   }

   @Override
   public String makeFilter(String varName, int valueIndex) {
      return new StringBuilder()
         .append(varName)
         .append(" = \"")
         .append(mDomain.get(valueIndex))
         .append("\"")
         .toString();
   }

   @Override
   public List<String> getStringValues() {
      return mDomain;
   }

}
