package airldm2.core;

import java.util.Vector;

import airldm2.exceptions.RTConfigException;

/**
 * 
 * A covenience data structure to hold a bunch of LDInstance in memory
 * 
 * @author neeraj (neeraj@cs.iastate.edu)
 * @since Feb 11, 2008
 * @version $Date: 2008/02/13 07:14:40 $
 */
public class LDTestInstances {
   /**
    * The Data Descriptor for this instance
    */
   DataDescriptor desc;

   /**
    * The instances. The value of each instance is stored as Vector<String>
    */
   Vector<Vector<String>> instances = new Vector<Vector<String>>();

   /**
    * Gets an LDInstance from this data set as indicated by index (starts
    * from zero)
    * 
    * @param index
    */
   public LDInstance getLDInstance(int index) throws RTConfigException {

      boolean labeled = true; // test instances always labeled
      return new LDInstance(desc, instances.get(index), labeled);
   }

   public LDTestInstances(DataDescriptor desc) {
      this.desc = desc;

   }

   public int getNumberInstances() {
      return instances.size();
   }

   public void addInstance(Vector<String> currInstance) {
      instances.add(currInstance);
   }

   public void load(String arffFileName) {
   }

}
