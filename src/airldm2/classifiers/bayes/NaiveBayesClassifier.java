package airldm2.classifiers.bayes;

import java.util.Arrays;
import java.util.Vector;

import weka.core.Instance;
import airldm2.classifiers.Classifier;
import airldm2.core.ISufficentStatistic;
import airldm2.core.LDInstance;
import airldm2.core.LDInstances;
import airldm2.core.SSDataSource;
import airldm2.core.datatypes.relational.ColumnDescriptor;
import airldm2.core.datatypes.relational.SingleRelationDataDescriptor;
import airldm2.util.MathUtil;
import airldm2.util.AttribValuePair;

public class NaiveBayesClassifier extends Classifier {

   private boolean DEBUG = true;

   // String[] attribNames;
   // String[][] attribValues;
   private SingleRelationDataDescriptor mDataDesc;

   private int mNumInstances;

   //[attribute name][class value][attribute value]
   private double[][][] mCounts;

   //[class value]
   private double[] mClassCounts;

   
   public void buildClassifier(LDInstances instances) throws Exception {
      assert (instances != null);

      boolean noMissingValues = false; // default Assumption

      // check options to see if the missing
      if (this.containsOption("?")) {
         noMissingValues = true;
      }
      mDataDesc = (SingleRelationDataDescriptor) instances.getDesc();
      SSDataSource dataSource = instances.getDataSource();

      Vector<ColumnDescriptor> allAttributes = mDataDesc.getTableDesc().getColumns();

      // We assume last attribute is the classIndex
      ColumnDescriptor classAttribute = allAttributes.lastElement();

      Vector<String> classLabels = classAttribute.getPossibleValues();
      int numOfClassLabels = classLabels.size();
      mClassCounts = new double[numOfClassLabels];

      // Allocate Memory. We have to have memory to store for a given
      // class,
      // for a given attributevalue for a given attribute

      // subtract one as do not include class attribute
      int numAttributes = allAttributes.size() - 1;

      // [attribute name][class label][attribute value]
      mCounts = new double[numAttributes][numOfClassLabels][];
      
      // find possible values for each attribute and allocate memory
      // we assume last index is class Attribute, hence loop only to
      // numAttributes (which is allAttributes.size() - 1)
      for (int i = 0; i < numAttributes; i++) {
         int numValuesCurrAttrib = allAttributes.get(i).getNumValues();
         for (int j = 0; j < numOfClassLabels; j++) {
            mCounts[i][j] = new double[numValuesCurrAttrib];
         }
      }

      ColumnDescriptor currAttribute;
      String currClassLabel;
      String currAttributeValue;
      Vector<String> currAttributeValues;

      // store two attribValuePairs-One each for classLabel AND
      // Attributevalue
      AttribValuePair[] classAndAttribute = new AttribValuePair[2];

      for (int i = 0; i < numAttributes; i++) {
         currAttribute = allAttributes.get(i);
         currAttributeValues = currAttribute.getPossibleValues();
         for (int j = 0; j < classLabels.size(); j++) {
            currClassLabel = classLabels.get(j);
            for (int k = 0; k < currAttributeValues.size(); k++) {
               currAttributeValue = currAttributeValues.get(k);
               classAndAttribute[0] = new AttribValuePair(classAttribute
                     .getColumnName(), currClassLabel);
               classAndAttribute[1] = new AttribValuePair(currAttribute
                     .getColumnName(), currAttributeValue);
               ISufficentStatistic tempSuffStat = dataSource
                     .getSufficientStatistic(classAndAttribute);
               mCounts[i][j][k] = tempSuffStat.getValue().intValue();

               /*
                * query = "SELECT count(*) FROM " +
                * dataDesc.getRelationName() + " WHERE `" +
                * attribNames[classIndex] + "`='" +
                * attribValues[classIndex][j] + "' AND `" + attribNames[i]
                * + "`='" + attribValues[i][k] + "'"; counts[i][j][k] =
                * dataSource.getSufficientStatistic(query)
                * .getValue().intValue() + 1;
                */
               /*
                * Debug("counts=(" + i + "," + j + "," + k + ")-->" +
                * counts[i][j][k] + "\n");
                */
            }
         }
      }
      
      /**
       * calculate Class Counts. If no missing values in dataset we can
       * calculate by summing over all possible values of an attribute for
       * the class label for which we are calculating counts
       */
      if (noMissingValues) {
         // we can chose any attribute to
         // calculate counts. In future this
         // may be a parameter
         int attributeIndexUsed = 0;
         int possibleValuesforAttribute = allAttributes.get(attributeIndexUsed).getNumValues();
         for (int c = 0; c < numOfClassLabels; c++) {
            for (int a = 0; a < possibleValuesforAttribute; a++) {
               mClassCounts[c] += mCounts[attributeIndexUsed][c][a];
            }
         }

      } else {
         // since missing values are present we need to explictly ask
         // queries for class counts
         AttribValuePair nameValue = new AttribValuePair();
         nameValue.setAttribName(classAttribute.getColumnName());
         // get counts for all possible class labels
         for (int index = 0; index < numOfClassLabels; index++) {

            // set the current class Label
            nameValue.setAttribValue(classLabels.get(index));
            ISufficentStatistic tempSuffStat = dataSource
                  .getSufficientStatistic(nameValue);
            mClassCounts[index] = tempSuffStat.getValue().intValue();
            // Debug(nameValue.getAttribName() + "=" +
            // nameValue.getAttribValue() + " has Count " +
            // classCounts[index]);

            if (mClassCounts[index] == 0) {
               System.out
                     .println(nameValue.getAttribName()
                           + "="
                           + nameValue.getAttribValue()
                           + " has Count "
                           + mClassCounts[index]
                           + "  . Zero count may result in exception when computing distribution for instance. Check configurations");
            }

         }
      }

      /**
       * Calculate Number of Instances
       */
      if (noMissingValues) {
         for (int i = 0; i < numOfClassLabels; i++) {
            mNumInstances += mClassCounts[i];
         }

      } else {

         // get number of instances from an explicit query.
         // Remark: How we calculated instances above will work for both
         // cases, since we calculated class counts appropriately.
         // Removing Will save 1 query for NB and more for decison trees
         // TODO: Make this a query too so that pattern works for decison
         // tree also
         mNumInstances = dataSource.getNumberInstances();
      }

      if (DEBUG) {
         this.dumpClassCounts(System.out);
      }

      // Handle Missing Values by Imputation at Sufficient Stat level
      mCounts = airldm2.filters.attribute.ReplaceMissingValues.SpreadMissingValues(mCounts, mNumInstances);
   }

   public double[] distributionForInstance(LDInstance instance) {
      return distributionForInstance(instance.getLocation());      
   }
   
   public double[] distributionForInstance(Instance instance) {
      // for the passed instance the attributes has some value and what is
      // the index of this value in the count structure
      double[] instValsDouble = instance.toDoubleArray();
      int[] instVals = MathUtil.castToInt(instValsDouble);
      return distributionForInstance(instVals);
   }

   private double[] distributionForInstance(int[] instVals) {
      double[] dist = new double[mClassCounts.length];
      Arrays.fill(dist, 1.0);
      
      /**
       * Set number of attributes. It can also be got from desc as int
       * numAttributes = dataDesc.getTableDesc().getColumns().size() - 1;
       */
      int numAttributes = mCounts.length;

      //calculate the dist for each class value
      for (int c = 0; c < dist.length; c++) {// size: # of class values
         for (int a = 0; a < numAttributes; a++) {
            //With Laplace correction
            dist[c] *= (mCounts[a][c][instVals[a]] + 1) / (mClassCounts[c] + mCounts[a][c].length);
         }
         //With Laplace correction
         dist[c] *= (mClassCounts[c] + 1) / (mNumInstances + mClassCounts.length);
      }

      MathUtil.normalize(dist);
      
      return dist;
   }

   public double classifyInstance(LDInstance instance) {
      double[] dist = distributionForInstance(instance);
      return MathUtil.maxIndex(dist);
   }
   
   public double classifyInstance(Instance instance) {
      double[] dist = distributionForInstance(instance);
      return MathUtil.maxIndex(dist);
   }
   
   private void Debug(String debug) {
      if (DEBUG) {
         System.out.println(debug);
      }
   }

   private void dumpClassCounts(java.io.PrintStream out) {
      out.println("Number Of Instances=" + mNumInstances);
      out.println("****Class Counts****");
      for (int i = 0; i < mClassCounts.length; i++) {
         out.println("class" + i + ":=" + mClassCounts[i]);
      }
   }

   public static void main(String[] args) {
      NaiveBayesClassifier classifier = new NaiveBayesClassifier();
      runClassifier(classifier, args);
   }

}
