package airldm2.classifiers.rl;

import airldm2.core.LDInstances;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;

public class InstanceAggregator {

   public static AggregatedInstance[] aggregate(LDInstances testInstances) {
      RDFDataSource mDataSource = (RDFDataSource) testInstances.getDataSource();
      RDFDataDescriptor mDataDesc = (RDFDataDescriptor) testInstances.getDesc();
      
      
      //fire sparql queries
      //look up value index
      
      
      return null;      
   }
   
}
