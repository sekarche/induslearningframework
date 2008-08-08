/**
 * THIS CODE IS STILL INCOMPLETE Id3Classifier.java<br>
 * TODO Write description for Id3Classifier.java.
 * 
 * $Header:
 * /home/CVS/airldm2/src/airldm2/classifiers/trees/Id3Classifier.java,v 1.1
 * 2008/03/26 20:29:04 kansas Exp $
 */

package airldm2.classifiers.trees;

import java.util.Vector;

import weka.core.Utils;
import airldm2.classifiers.Classifier;
import airldm2.core.DefaultOptionHandlerImpl;
import airldm2.core.ISufficentStatistic;
import airldm2.core.LDInstance;
import airldm2.core.LDInstances;
import airldm2.core.SSDataSource;
import airldm2.core.datatypes.relational.ColumnDescriptor;
import airldm2.core.datatypes.relational.SingleRelationDataDescriptor;
import airldm2.util.AttribValuePair;

/**
 * Id3Classifier implements the Id3 decision tree learning algorithm to
 * operate on relation data. It is written to mimic its Weka counterpart
 * 
 * @author Vikas Bahirwani (vikas@ksu.edu, vikasbahirwani@gmail.com)
 * @since Mar 26, 2008
 * @version $Date: 2008/08/04 16:06:30 $
 */
public class Id3Classifier extends DefaultOptionHandlerImpl implements
      Classifier {

   /** The node's successors */
   private Id3Classifier[] m_Successors;

   private static int count = 0;

   /** Attribute used for splitting. */
   private ColumnDescriptor m_Attribute;

   /** Class value if node is leaf. */
   private String m_ClassValue;

   /** Class distribution if node is leaf. */
   private double[] m_Distribution;

   boolean DEBUG = false;

   static SSDataSource dataSource;

   static int numInstances;

   static double[] classCounts;

   static int numOfClassLabels;

   static Vector<String> classLabels;

   static SingleRelationDataDescriptor dataDesc;

   static Vector<ColumnDescriptor> allAttributes;

   static ColumnDescriptor classAttribute;

   // @Override
   public void buildClassifier(LDInstances instances) throws Exception {
      assert (instances != null);
      dataDesc = (SingleRelationDataDescriptor) instances.getDesc();
      dataSource = instances.getDataSource();
      numInstances = dataSource.getNumberInstances();
      allAttributes = dataDesc.getTableDesc().getColumns();

      // We assume last attribute is the classIndex
      classAttribute = allAttributes.lastElement();
      classLabels = classAttribute.getPossibleValues();
      numOfClassLabels = classLabels.size();

      // Computing class counts
      classCounts = new double[numOfClassLabels];
      AttribValuePair nameValue = new AttribValuePair();
      nameValue.setAttribName(classAttribute.getColumnName());
      // get counts for all possible class labels
      for (int index = 0; index < numOfClassLabels; index++) {
         // set the current class Label
         nameValue.setAttribValue(classLabels.get(index));
         ISufficentStatistic tempSuffStat = dataSource
               .getSufficientStatistic(nameValue);
         classCounts[index] = tempSuffStat.getValue().intValue();
      }

      // TODO check whether input contains only nominal values

      AttribValuePair[] classAndAttributes = new AttribValuePair[1];
      AttribValuePair classAttribValue = new AttribValuePair();
      classAttribValue.setAttribName(classAttribute.getColumnName());
      classAndAttributes[0] = classAttribValue;
      makeTree(classAndAttributes);
      Debug(Id3Classifier.count + " Queries requested");
      System.out.println(Id3Classifier.count + " requested");

      /*
       * FOLLOWING CODE MAY BE USED FOR TESTING INDIVIDUAL FUNCTIONS
       * 
       * AttribValuePair[] classAndAttributes = new AttribValuePair[1];
       * AttribValuePair classAttribValue = new AttribValuePair();
       * classAttribValue.setAttribName(classAttribute.getColumnName());
       * classAndAttributes[0] = classAttribValue;
       * computeEntropy(classAndAttributes); ColumnDescriptor newAttrib =
       * allAttributes.firstElement();
       * System.out.println(">>>>>>>>>>>>>>>>>>" +
       * newAttrib.getColumnName()); Vector<String> newValues =
       * newAttrib.getPossibleValues(); int size = newValues.size();
       * AttribValuePair[] newAttribValues = new AttribValuePair[size];
       * AttribValuePair temp; for (int i = 0; i < newValues.size(); i++) {
       * temp = new AttribValuePair(newAttrib.getColumnName(),
       * newValues.get(i)); newAttribValues[i] = temp; }
       * computeInfoGain(classAndAttributes, newAttribValues);
       */

   }

   /**
    * 
    * @param usedAttribValueSet set of attribute-value pairs used to reach
    * the current node in the tree. (Even for an empty or null tree) The
    * first attribute of usedAttribValueSet should be the classifying
    * attribute with no values associated with it.
    * @throws Exception
    */
   private void makeTree(AttribValuePair[] usedAttribValueSet) throws Exception {
      ISufficentStatistic tempSuffStat;
      int numberOfInstances = 0;
      if (usedAttribValueSet == null) {
         System.err.println("Cannot make the decision tree");
         System.exit(0);
      } else if (usedAttribValueSet.length == 1) {
         numberOfInstances = dataSource.getNumberInstances();
      } else {
         m_Distribution = new double[numOfClassLabels];
         for (int i = 0; i < numOfClassLabels; i++) {
            usedAttribValueSet[0].setAttribValue(classLabels.get(i));
            tempSuffStat = dataSource
                  .getSufficientStatistic(usedAttribValueSet);
            Id3Classifier.count++;
            m_Distribution[i] = tempSuffStat.getValue().intValue();
            numberOfInstances += m_Distribution[i];
         }
      }
      Debug("Inside makeTree: numberOfInstances=" + numberOfInstances);
      if (numberOfInstances == 0) {
         m_Attribute = null;
         m_ClassValue = ColumnDescriptor.DEFAULT_MISSING_VALUE;
         /*
          * m_Distribution[i] for 0<=i<numOfClassLabels is already
          * assigned values
          */
         return;
      }

      // Compute attribute with maximum information gain
      double[] infoGains = new double[allAttributes.size() - 1];
      AttribValuePair[] newAttribValues;
      ColumnDescriptor tempAttrib;
      Vector<String> possibleValues;
      double maxInfoGain = 0;
      // iterate through all attributes except the class
      for (int i = 0; i < allAttributes.size() - 1; i++) {
         tempAttrib = allAttributes.get(i);
         if (contains(usedAttribValueSet, tempAttrib)) {
            infoGains[i] = 0;
         } else {
            possibleValues = tempAttrib.getPossibleValues();
            /*
             * Debug("Inside makeTree(): Attribute selected for computing
             * information gain =" + tempAttrib.getColumnName());
             */
            newAttribValues = new AttribValuePair[tempAttrib.getNumValues()];
            for (int j = 0; j < tempAttrib.getNumValues(); j++) {
               newAttribValues[j] = new AttribValuePair(tempAttrib
                     .getColumnName(), possibleValues.get(j));
            }
            infoGains[i] = computeInfoGain(usedAttribValueSet,
                  numberOfInstances, newAttribValues);

         }
         Debug("Inside makeTree(): Attribute Index=" + i + " Attribute  Name="
               + tempAttrib.getColumnName() + " Info Gain=" + infoGains[i]);
      }

      maxInfoGain = infoGains[Utils.maxIndex(infoGains)];
      m_Attribute = allAttributes.get(Utils.maxIndex(infoGains));
      Debug("Inside makeTree(): Attribute with maximum information gain is "
            + m_Attribute.getColumnName() + " infoGain =" + maxInfoGain);
      if (maxInfoGain == 0) {
         m_Attribute = null;
         // m_Distribution already contains the desired distribution
         /*
          * m_Distribution = new double[numOfClassLabels];
          * ISufficentStatistic tempStat; for (int i = 0; i <
          * numOfClassLabels; i++) {
          * usedAttribValueSet[0].setAttribValue(classLabels.elementAt(i));
          * tempStat =
          * dataSource.getSufficientStatistic(usedAttribValueSet);
          * m_Distribution[i] = tempStat.getValue(); }
          */
         if (DEBUG) {
            for (int i = 0; i < numOfClassLabels; i++) {
               Debug("Inside makeTree(): m_Distribution[" + i + "]="
                     + m_Distribution[i]);
            }
         }
         Utils.normalize(m_Distribution);
         m_ClassValue = classLabels.elementAt(Utils.maxIndex(m_Distribution));
      } else {
         /*
          * Instances[] splitData = splitData(data, m_Attribute). Following
          * segment of code mimics this statement of Id3 in Weka
          */
         AttribValuePair[] newAttribValueSet = new AttribValuePair[usedAttribValueSet.length + 1];
         for (int i = 0; i < usedAttribValueSet.length; i++) {
            newAttribValueSet[i] = usedAttribValueSet[i];
         }
         newAttribValueSet[usedAttribValueSet.length] = new AttribValuePair(
               m_Attribute.getColumnName(), "");
         m_Successors = new Id3Classifier[m_Attribute.getNumValues()];
         possibleValues = m_Attribute.getPossibleValues();
         for (int j = 0; j < m_Attribute.getNumValues(); j++) {
            newAttribValueSet[usedAttribValueSet.length]
                  .setAttribValue(possibleValues.get(j));
            m_Successors[j] = new Id3Classifier();
            m_Successors[j].makeTree(newAttribValueSet);
         }
      }
   }

   public boolean contains(AttribValuePair[] usedAttribs,
         ColumnDescriptor newAttrib) {
      for (int i = 0; i < usedAttribs.length; i++) {
         if ((usedAttribs[i].getAttribName()).equalsIgnoreCase(newAttrib
               .getColumnName())) {
            Debug("newAttrib " + newAttrib.getColumnName() + "already in use");
            return true;
         }
      }
      return false;
   }

   /**
    * 
    * @param classAndOldAttribSet list of old attribute value pairs used to
    * produce a subset of data for computing Information Gain
    * @param newAttribValues the new attribute and its values for which
    * Information Gain is to be computed on the subset of data
    * @return Information Gain for the subset of data based on the
    * newAttribValues
    * @throws Exception
    */

   private double computeInfoGain(AttribValuePair[] classAndOldAttribSet,
         int oldCount, AttribValuePair[] newAttribValues) throws Exception {
      if (classAndOldAttribSet == null) {
         System.out.println("Cannot compute information gain");
         return -1;
      } else {

         int newCount, newSetSize;
         ISufficentStatistic tempSuffStat;
         // TODO this old count might be already computed
         /*
          * int oldCount=0; for (int i = 0; i < numOfClassLabels; i++) {
          * classAndOldAttribSet[0].setAttribValue(classLabels.elementAt(i));
          * tempSuffStat = dataSource
          * .getSufficientStatistic(classAndOldAttribSet); oldCount +=
          * tempSuffStat.getValue().intValue(); }
          */

         double infoGain = computeEntropy(classAndOldAttribSet, oldCount);
         // Debug("oldCount= " + oldCount);
         newSetSize = classAndOldAttribSet.length + 1;
         AttribValuePair[] newAttribSet = new AttribValuePair[newSetSize];
         for (int i = 0; i < classAndOldAttribSet.length; i++) {
            newAttribSet[i] = classAndOldAttribSet[i];
            newAttribSet[i] = classAndOldAttribSet[i];
         }
         newAttribSet[newSetSize - 1] = new AttribValuePair(newAttribValues[0]
               .getAttribName(), null);
         double newEntropy;
         for (int i = 0; i < newAttribValues.length; i++) {
            newAttribSet[newSetSize - 1].setAttribValue(newAttribValues[i]
                  .getAttribValue());
            newCount = 0;
            for (int j = 0; j < numOfClassLabels; j++) {
               newAttribSet[0].setAttribValue(classLabels.elementAt(j));
               tempSuffStat = dataSource.getSufficientStatistic(newAttribSet);
               Id3Classifier.count++;
               newCount += tempSuffStat.getValue().intValue();
            }

            if (newCount > 0) {
               newEntropy = computeEntropy(newAttribSet, newCount);
               // Debug("newCount= " + newCount);
               infoGain -= ((double) newCount / oldCount) * newEntropy;
            }
         }
         // Debug("InfoGain= " + infoGain);
         return infoGain;
      }
   }

   /**
    * 
    * @param classAndAttributes list of attribute value pairs (beginning
    * with the class attribute) according to which a subset of data is to
    * be chosen for computing Entropy
    * @return entropy for the subset of data specified by
    * classAndAttributes
    * @throws Exception
    */
   private double computeEntropy(AttribValuePair[] classAndAttributes, int size)
         throws Exception {
      if (classAndAttributes == null) {
         System.err.println("Entropy cannot be computed");
         /* Debug("Entropy= " + -1); */
         return -1;
      } else if (classAndAttributes.length == 1) {
         double entropy = 0, temp = 0;
         for (int j = 0; j < numOfClassLabels; j++) {
            if (classCounts[j] > 0) {
               /*
                * temp = classCounts[j] * Utils.log2(classCounts[j]);
                * 
                * Debug("Class Count " + j + " = " + classCounts[j]);
                * Debug("temp=" + temp);
                */
               entropy -= classCounts[j] * Utils.log2(classCounts[j]);
            }
         }
         entropy /= size;
         entropy += Utils.log2(size);
         /* Debug("Entropy= " + entropy); */
         return entropy;

      } else {
         int tempClassCount, total = 0;
         ISufficentStatistic tempSuffStat;
         double entropy = 0;
         for (int j = 0; j < numOfClassLabels; j++) {
            classAndAttributes[0].setAttribValue(classLabels.elementAt(j));
            tempSuffStat = dataSource
                  .getSufficientStatistic(classAndAttributes);
            Id3Classifier.count++;
            tempClassCount = tempSuffStat.getValue().intValue();
            if (tempClassCount > 0) {
               entropy -= tempClassCount * Utils.log2(tempClassCount);
               total += tempClassCount;
            }
         }
         entropy /= total;
         entropy += Utils.log2(total);
         /*
          * Debug("Inside computeEntropy: data.numInstances=" + total + "
          * Entropy=" + entropy);
          */
         return entropy;
      }

   }

   /*
    * (non-Javadoc)
    * 
    * @see airldm2.classifiers.Classifier#classifyInstance(airldm2.core.LDInstance)
    */
   // @Override
   public double classifyInstance(LDInstance instance) throws Exception {
      // TODO Correct and Finish this code
      double[] dist = distributionForInstance(instance);
      double index = -1;
      double max = -1;

      for (int i = 0; i < dist.length; ++i) {
         if (dist[i] > max) {
            max = dist[i];
            index = i;
         }
      }
      return index;
   }

   /*
    * (non-Javadoc)
    * 
    * @see airldm2.classifiers.Classifier#distributionForInstance(airldm2.core.LDInstance)
    */
   // @Override
   public double[] distributionForInstance(LDInstance instance)
         throws Exception {
      // TODO check if an instance has missing value
      /*
       * CODE SEGMENT FROM WEKA if (instance.hasMissingValue()) { throw new
       * NoSupportForMissingValuesException( "Id3: no missing values, " +
       * "please."); }
       */
      if (m_Attribute == null) {
         return m_Distribution;
      } else {
         String m_Value = instance.getValue(m_Attribute);

         return m_Successors[m_Attribute.getIndex(m_Value)]
               .distributionForInstance(instance);
      }
   }

   /**
    * This function converts the Id3 into a printable string format. This
    * function mimics the operation of its Weka counterpart
    */
   @Override
   public String toString() {

      if ((m_Distribution == null) && (m_Successors == null)) {
         return "Id3: No model built yet.";
      }
      return "Id3\n\n" + toString(0);
   }

   private String toString(int level) {

      StringBuffer text = new StringBuffer();

      if (m_Attribute == null) {
         if (m_ClassValue.equals(ColumnDescriptor.DEFAULT_MISSING_VALUE)) {
            text.append(": null");
         } else {
            text.append(": " + m_ClassValue);
         }
      } else {
         Vector<String> possibleValues = m_Attribute.getPossibleValues();
         for (int j = 0; j < m_Attribute.getNumValues(); j++) {
            text.append("\n");
            for (int i = 0; i < level; i++) {
               text.append("|  ");
            }
            text.append(m_Attribute.getColumnName() + " = "
                  + possibleValues.elementAt(j));
            text.append(m_Successors[j].toString(level + 1));
         }
      }
      return text.toString();
   }

   /**
    * @param args
    */
   public static void main(String[] args) {
      // TODO Auto-generated method stub

   }

   private void Debug(String debug) {
      if (DEBUG) {
         System.out.println(debug);
      }
   }

}