package airldm2.core;

import java.util.Vector;

import airldm2.exceptions.RTConfigException;

public class LDInstanceRDF extends LDInstance {
   
   public LDInstanceRDF(DataDescriptor desc, Vector<String> values, boolean labeled)
   throws RTConfigException {
      super(desc,values,labeled);

   }
   public String getClassLabel() throws Exception {
      //RDFDataDescriptor rdfDesc
      return null;
   }

}
