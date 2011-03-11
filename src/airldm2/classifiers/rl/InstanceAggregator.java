package airldm2.classifiers.rl;

import java.util.List;

import org.openrdf.model.URI;

import airldm2.core.LDInstances;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.util.CollectionUtil;

public class InstanceAggregator {

   public static List<AggregatedInstance> aggregate(LDInstances instances) {
      RDFDataSource dataSource = (RDFDataSource) instances.getDataSource();
      RDFDataDescriptor dataDesc = (RDFDataDescriptor) instances.getDesc();
      
      List<URI> instanceURIs = dataSource.getTargetInstances(dataDesc.getTargetType());
      List<AggregatedInstance> aggInstances = CollectionUtil.makeList();
      for (URI instanceURI : instanceURIs) {
         int[][] featureValueIndexCount = null;
         int[] targetValueIndexCount = null;
         
         //use RbcAttribute
         //fire sparql queries
         //look up value index
         
         AggregatedInstance aggInstance = new AggregatedInstance(featureValueIndexCount, targetValueIndexCount);
         aggInstances.add(aggInstance);
      }
      
      return aggInstances;      
   }
   
}
