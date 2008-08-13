package airldm2.classifiers.bayes;

import java.util.Vector;

import weka.core.Instance;
import airldm2.classifiers.Classifier;
import airldm2.core.DefaultOptionHandlerImpl;
import airldm2.core.ISufficentStatistic;
import airldm2.core.LDInstance;
import airldm2.core.LDInstances;
import airldm2.core.SSDataSource;
import airldm2.core.datatypes.relational.ColumnDescriptor;
import airldm2.core.datatypes.relational.SingleRelationDataDescriptor;
import airldm2.util.AttribValuePair;

public class NaiveBayesClassifier extends  Classifier {

   boolean DEBUG = true;

   SSDataSource dataSource;

   int numInstances;

   int classIndex;

   /*
    * It goes: [attribute name][class value][attribute value]
    */

   double[][][] counts;

   double[] classCounts;

   // String[] attribNames;
   // String[][] attribValues;
   SingleRelationDataDescriptor dataDesc;

   public void buildClassifier(LDInstances instances) throws Exception {
      assert (instances != null);

      boolean noMissingValues = false; // default Assumption

      // check options to see if the missing
      if (this.containsOption("?")) {
         noMissingValues = true;
      }
      dataDesc = (SingleRelationDataDescriptor) instances.getDesc();
      SSDataSource dataSource = instances.getDataSource();

      Vector<ColumnDescriptor> allAttributes = dataDesc.getTableDesc()
            .getColumns();

      // We assume last attribute is the classIndex
      ColumnDescriptor classAttribute = allAttributes.lastElement();

      Vector<String> classLabels = classAttribute.getPossibleValues();
      int numOfClassLabels = classLabels.size();
      classCounts = new double[numOfClassLabels];

      // Allocate Memory. We have to have memory to store for a given
      // class,
      // for a given attributevalue for a given attribute

      // subtract one as do not include class attribute
      int numAttributes = allAttributes.size() - 1;

      // [attribute name][class label][attribute value]
      counts = new double[numAttributes][numOfClassLabels][];

      // find possible values for each attribute and allocate memory
      // we assume last index is class Attribute, hence loop only to
      // numAttributes (which is allAttributes.size() - 1)
      for (int i = 0; i < numAttributes; i++) {
         int numValuesCurrAttrib = allAttributes.get(i).getNumValues();
         for (int j = 0; j < numOfClassLabels; j++) {
            counts[i][j] = new double[numValuesCurrAttrib];
         }
      }

      // TODO: Include Laplace Correction

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
               counts[i][j][k] = tempSuffStat.getValue().intValue();
               /*
                * query = "SELECT count(*) FROM " +
                * dataDesc.getRelationName() + " WHERE `" +
                * attribNames[classIndex] + "`='" +
                * attribValues[classIndex][j] + "' AND `" + attribNames[i] +
                * "`='" + attribValues[i][k] + "'"; counts[i][j][k] =
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
         int attributeIndexUsed = 0; // we can chose any attribute to
         // calculate counts. In future this
         // may be a parameter
         int possibleValuesforAttribute = allAttributes.get(attributeIndexUsed)
               .getNumValues();
         for (int index = 0; index < numOfClassLabels; index++) {
            for (int j = 0; j < possibleValuesforAttribute; j++)
               classCounts[index] += counts[attributeIndexUsed][index][j];

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
            classCounts[index] = tempSuffStat.getValue().intValue();
            // Debug(nameValue.getAttribName() + "=" +
            // nameValue.getAttribValue() + " has Count " +
            // classCounts[index]);
         }
      }

      /**
       * Calculate Number of Instances
       */
      if (noMissingValues) {
         for (int i = 0; i < numOfClassLabels; i++) {
            numInstances += classCounts[i];
         }

      } else {

         // get number of instances from an explicit query.
         // Remark: How we calculated instances above will work for both
         // cases, since we calculated class counts appropriately.
         // Removing Will save 1 query for NB and more for decison trees
         // TODO: Make this a query too so that pattern works for decison
         // tree also
         numInstances = dataSource.getNumberInstances();
      }

      if (DEBUG) {
         this.dumpClassCounts(System.out);
      }

      // Handle Missing Values by Imputation at Sufficient Stat level
      counts = airldm2.filters.attribute.ReplaceMissingValues
            .SpreadMissingValues(counts, numInstances);

      // TODO: Handle Laplace-->This will lead to change of number of
      // Instances
   }

   public double classifyInstance(LDInstance instance) {
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

   public double[] distributionForInstance(LDInstance instance) {
      int[] instVals = instance.getLocation();
      double[] dist = new double[classCounts.length];
      double sumDist = 0.0;
      int tempDouble;

      /**
       * Set number of attributes. It can also be got from desc as int
       * numAttributes = dataDesc.getTableDesc().getColumns().size() - 1;
       */
      int numAttributes = counts.length;

      /*
       * calculate the dist for each class value
       */
      for (int i = 0; i < dist.length; i++) {// size: # of class values
         double tempProb = 1.0;
         for (int j = 0; j < numAttributes; j++) {
            tempDouble = instVals[j];
            tempProb *= counts[j][i][tempDouble] / classCounts[i];
         }
         tempProb *= classCounts[i] / numInstances;
         dist[i] = tempProb;
         sumDist += tempProb;
      }

      // normalize
      for (int i = 0; i < dist.length; i++) {
         dist[i] /= sumDist;
      }

      return dist;
   }

   public double classifyInstance(Instance instance) {
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

   public double[] distributionForInstance(Instance instance) {

      // for the passed instance the attributes has some value and what is
      // the
      // index of this value
      // in the count structure
      double[] instVals = instance.toDoubleArray();
      double[] dist = new double[classCounts.length];
      double sumDist = 0.0;
      Double tempDouble;
      /**
       * Set number of attributes. It can also be got from desc as int
       * numAttributes = dataDesc.getTableDesc().getColumns().size() - 1;
       */
      int numAttributes = counts.length;

      /*
       * calculate the dist for each class value
       */
      for (int i = 0; i < dist.length; ++i) {// size: # of class values
         double tempProb = 1.0;
         for (int j = 0; j < numAttributes; j++) {
            tempDouble = instVals[j];
            tempProb *= counts[j][i][tempDouble.intValue()] / classCounts[i];
         }
         tempProb *= classCounts[i] / numInstances;
         dist[i] = tempProb;
         sumDist += tempProb;
      }

      // normalize
      for (int i = 0; i < dist.length; ++i) {
         dist[i] /= sumDist;
      }

      return dist;
   }

   private void Debug(String debug) {
      if (DEBUG) {
         System.out.println(debug);
      }
   }

   private void dumpClassCounts(java.io.PrintStream out) {

      out.println("Number Of Instances=" + numInstances);
      out.println("****Class Counts****");
      for (int i = 0; i < classCounts.length; i++) {
         out.println("class" + i + ":=" + classCounts[i]);
      }

   }

}
