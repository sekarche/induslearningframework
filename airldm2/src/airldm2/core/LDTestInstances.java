package airldm2.core;

import java.util.Vector;

import airldm2.exceptions.RTConfigException;
import airldm2.exceptions.NotImplementedException;

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

   
   double[] classLabelLocations;
   
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
   
   /**
    * Returns the classLabel locations in the all the instances. The class label location is the index
    * of the value of the curr class label in the possible class labels
    * @return
    * @throws RTConfigException
    */
   public double[] getClassLabelLocations() throws Exception{
      if(classLabelLocations != null) {
         return classLabelLocations;
      }
      
      int size = getNumberInstances();
      classLabelLocations = new double[size];
      LDInstance currInstance;
      for (int i =0; i < size; i++ ) {
         currInstance = getLDInstance(i);
         classLabelLocations[i] = currInstance.getClassValueLocation();
      }
      
      return classLabelLocations;
   }
   
   /**
    * Returns the descriptor for the testInstances.
    * It should be same as what the classifier is trained on
    * @return
    */
   public DataDescriptor getDesc() {
      return this.desc;
   }

   public void load(String arffFileName) throws  NotImplementedException {
      throw new airldm2.exceptions.NotImplementedException(0, arffFileName);
   }

}
