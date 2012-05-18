package airldm2.core.rl;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.openrdf.model.Value;

import airldm2.util.StringUtil;

public class NominalType implements DiscreteType {

   public static String NAME = "NOMINAL";
   
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

   @Override
   public String toString() {
      return NAME + "=" + StringUtil.toCSV(mDomain);
   }

   @Override
   public void write(Writer out) throws IOException {
      out.write(NAME);
      out.write("=");
      out.write(StringUtil.toCSV(mDomain));
   }

}
