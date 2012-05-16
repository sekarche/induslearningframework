package airldm2.database.rdf;

import static airldm2.util.StringUtil.angleBracket;
import static airldm2.util.StringUtil.makeContextPart;

import org.openrdf.model.URI;

import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RbcAttribute;
import airldm2.core.rl.ValueType;

public class QueryConstructor {

   protected RDFDataDescriptor mDesc;
   protected String mContextPart;
   
   public QueryConstructor(RDFDataDescriptor desc, String context) {
      mDesc = desc;
      mContextPart = makeContextPart(context);
   }
   
   protected String createInstanceFilter(URI instance) {
      return "FILTER(" + mDesc.getInstanceVar() + "=" + angleBracket(instance) + ")";
   }
   
   protected String createAttributeGraph(RbcAttribute att) {
      return "FILTER(" + mDesc.getInstanceVar() + "=" + att.getGraphPattern().getInstanceVar() + ") "
         + att.getGraphPattern();
   }

   protected String createValueFilter(RbcAttribute att, int valueIndex, String var) {
      ValueType valueType = att.getValueType();
      StringBuilder b = new StringBuilder();
      b.append("FILTER(")
         .append(valueType.makeFilter(var, valueIndex))
         .append(") ");
      return b.toString();
   }
   
   protected String createValueFilter(RbcAttribute att, int valueIndex) {
      return createValueFilter(att, valueIndex, att.getGraphPattern().getValueVar());
   }

}
