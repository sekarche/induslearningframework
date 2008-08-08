package airldm2.core;


/**
 * 
 * @author neeraj
 * 
 * Corresponds to a LargeDataInstances which cannot be stored in memory
 */
public class LDInstances {

   /*
    * A Data Source Which provides Sufficent Statistics acts as the
    * instances
    */
   private SSDataSource instances;

   /* Descriptor about data. Number of Instances/Attributes/Values */
   private DataDescriptor desc;

   /**
    * @param desc the desc to set
    */
   public void setDesc(DataDescriptor desc) {
      this.desc = desc;
   }

   /**
    * @return the desc
    */
   public DataDescriptor getDesc() {
      return desc;
   }

   /**
    * @param instances the datasource to set
    */
   public void setDataSource(SSDataSource instances) {
      this.instances = instances;
   }

   /**
    * @return the sufficent statistics data source instances
    */
   public SSDataSource getDataSource() {
      return instances;
   }

}
