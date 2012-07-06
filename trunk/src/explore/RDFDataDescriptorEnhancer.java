package explore;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openrdf.model.URI;
import org.openrdf.model.Value;

import airldm2.core.rl.EnumType;
import airldm2.core.rl.NominalType;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataDescriptorParser;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.database.rdf.SPARQLQueryResult;
import airldm2.exceptions.RDFDataDescriptorFormatException;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.util.CollectionUtil;

public class RDFDataDescriptorEnhancer {

   protected static Logger Log = Logger.getLogger("airldm2.classifiers.rl.RDFDataDescriptorEnhancer");
   static { Log.setLevel(Level.WARNING); }
   
   private RDFDataSource mDataSource;

   public RDFDataDescriptorEnhancer(RDFDataSource dataSource) {
      mDataSource = dataSource;
   }
   
   public void fillDomain(String inDescFile, String outDescFile) throws IOException, RDFDataDescriptorFormatException, RDFDatabaseException {
      RDFDataDescriptor desc = RDFDataDescriptorParser.parse(inDescFile);
      fillDomain(desc);
      BufferedWriter out = new BufferedWriter(new FileWriter(outDescFile));
      desc.write(out);
      out.close();
   }

   public void fillDomain(RDFDataDescriptor desc) throws RDFDatabaseException {
      URI targetType = desc.getTargetType();
      for (RbcAttribute a : desc.getAllAttributes()) {
         fillDomain(targetType, a);
      }
   }

   public void fillDomain(URI targetType, RbcAttribute a) throws RDFDatabaseException {
      if (a.getValueType() != null) return;
      
      SPARQLQueryResult result = mDataSource.getRangeOf(targetType, a);
      List<Value> range = result.getValueList();
      boolean allURIs = true;
      for (Value v : range) {
         if (!(v instanceof URI)) {
            allURIs = false;
            break;
         }
      }
      
      if (allURIs) {
         List<URI> uris = CollectionUtil.makeList();
         for (Value v : range) {
            uris.add((URI) v);
         }
         
         a.setValueType(new EnumType(uris));
         
      } else {
         List<String> strs = CollectionUtil.makeList();
         for (Value v : range) {
            strs.add(v.stringValue());
         }
         
         a.setValueType(new NominalType(strs));
         
      }
      
      Log.warning("domain size = " + range.size());
   }
   
}
