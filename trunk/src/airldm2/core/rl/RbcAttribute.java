package airldm2.core.rl;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openrdf.model.URI;

import airldm2.classifiers.rl.estimator.AttributeEstimator;
import airldm2.classifiers.rl.estimator.BernoulliEstimator;
import airldm2.classifiers.rl.estimator.CategoryEstimator;
import airldm2.classifiers.rl.estimator.ExponentialEstimator;
import airldm2.classifiers.rl.estimator.GaussianEstimator;
import airldm2.classifiers.rl.estimator.MultinomialEstimator;
import airldm2.core.rl.NumericType.Distribution;

/**
 * 
 *  A place holder for an attributes of interest in Relational Bayesian Classifier
 *
 * @author neeraj (neeraj.kaul@gmail.com)
 * @since Jan 24, 2011
 * @version $Date: $
 */
public class RbcAttribute {
   
   private String mName;
   private ValueType mValueType;
   private ValueAggregator mAggregatorType;
   private URI mHierarchyRoot;
   private GraphPattern mGraph;
   
   public RbcAttribute(String name, ValueType valueType, ValueAggregator aggregatorType, URI hierarchyRoot, GraphPattern graph) {
      mName = name;
      mValueType = valueType;
      mAggregatorType = aggregatorType;
      mHierarchyRoot = hierarchyRoot;
      mGraph = graph;
   }
      
   public String getName() {
      return mName;
   }
   
   public GraphPattern getGraphPattern() {
      return mGraph;
   }

   public ValueAggregator getAggregatorType() {
      return mAggregatorType;
   }
   
   public URI getHierarchyRoot() {
      return mHierarchyRoot;
   }
   
   public ValueType getValueType() {
      return mValueType;
   }
   
   public void setValueType(ValueType v) {
      mValueType = v;
   }
   
   public int getDomainSize() {
      return ((DiscreteType) getValueType()).domainSize();
   }
   
   public List<String> getDomain() {
      return ((DiscreteType) getValueType()).getStringValues();
   }

   public boolean isHierarchicalHistogram() {
      return (mValueType == null || mValueType instanceof EnumType)
         && mAggregatorType == ValueAggregator.HISTOGRAM
         && mHierarchyRoot != null;
   }
   
   public boolean isHierarchicalSet() {
      return (mValueType == null || mValueType instanceof EnumType)
         && mAggregatorType == ValueAggregator.SET
         && mHierarchyRoot != null;
   }
      
   public RbcAttribute extendWithHierarchy(URI node, boolean isLeaf) {
      return new RbcAttribute(mName, mValueType, mAggregatorType, mHierarchyRoot, mGraph.extendWithHierarchy(node, isLeaf));
   }
   
   public RbcAttribute extendGraphVariableName(int id) {
      return new RbcAttribute(mName, mValueType, mAggregatorType, mHierarchyRoot, mGraph.extendGraphVariableName(id));
   }
   
   public URI getExtendedHierarchy() {
      return mGraph.getExtendedHierarchy();
   }
   
   public AttributeEstimator getEstimator() {
      if (getValueType() instanceof DiscreteType) {
         if (getAggregatorType() == ValueAggregator.HISTOGRAM) {
            return new MultinomialEstimator(this);
         } else if (getAggregatorType() == ValueAggregator.SET) {
            return new BernoulliEstimator(this);
         } else {
            return new CategoryEstimator(this);
         }
      } else {
         NumericType nt = (NumericType) getValueType();
         Distribution dist = nt.getDist();
         if (dist == Distribution.EXPONENTIAL) {
            return new ExponentialEstimator(this);
         } else if (dist == Distribution.GAUSSIAN) {
            return new GaussianEstimator(this);
         } else if (dist == Distribution.POISSON) {
            throw new UnsupportedOperationException(Distribution.POISSON + " not yet implemented.");
         }
      }
      
      return null;
   }

   public RbcAttribute copy() {
      return new RbcAttribute(mName, mValueType, mAggregatorType, mHierarchyRoot, mGraph);
   }
   
   @Override
   public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
   }

   public void write(Writer out) throws IOException {
      out.write(RDFDataDescriptorParser.ATTRIBUTE);
      out.write(mName);
      out.write("\n");
      
      mValueType.write(out);
      out.write("\n");
      
      out.write(RDFDataDescriptorParser.AGGREGATOR);
      out.write(mAggregatorType.toString());
      out.write("\n");

      out.write(RDFDataDescriptorParser.HIERARCHY);
      out.write(mHierarchyRoot.stringValue());
      out.write("\n");
      
      out.write(mGraph.toString());
      out.write("\n");
   }
   
   @Override
   public int hashCode() {
      return mName.hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof RbcAttribute)) return false;
      RbcAttribute other = (RbcAttribute) obj;
      return mName.equals(other.mName);
   }
   
}