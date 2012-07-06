package airldm2.classifiers.rl;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jgrapht.graph.SimpleDirectedGraph;

import airldm2.classifiers.Classifier;
import airldm2.classifiers.rl.estimator.AttributeEstimator;
import airldm2.classifiers.rl.estimator.AttributeValue;
import airldm2.classifiers.rl.estimator.ClassEstimator;
import airldm2.classifiers.rl.estimator.GaussianEstimator;
import airldm2.classifiers.rl.estimator.MappedHistogram;
import airldm2.classifiers.rl.tree.TreeNodeSplitter;
import airldm2.core.LDInstance;
import airldm2.core.LDInstances;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.core.rl.RbcAttributeValue;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.util.CollectionUtil;
import airldm2.util.Timer;
import explore.RDFDataDescriptorEnhancer;


public class RDTClassifier extends Classifier {

   protected static Logger Log = Logger.getLogger("airldm2.classifiers.rl.RDTClassifier");
   static { Log.setLevel(Level.WARNING); }
   
   private static final double INFO_GAIN_THRESHOLD = 0.01;
   
   private RDFDataSource mDataSource;
   private RDFDataDescriptor mDataDesc;
   private ClassEstimator mClassEst;
   
   private int mNumOfClassLabels;
   private List<RbcAttributeValue> mAttributeValues;
   private SimpleDirectedGraph<TreeNodeSplitter,Boolean> mTree;
   private TreeNodeSplitter mRoot;
   
   private List<RbcAttributeValue> mTreeAttributeValues;

   private int mDepthLimit = Integer.MAX_VALUE;
   
   public RDTClassifier() {
   }
   
   public RDTClassifier(int depthLimit) {
      mDepthLimit = depthLimit;
   }
   
   @Override
   public void buildClassifier(LDInstances instances) throws Exception {
      mDataDesc = (RDFDataDescriptor) instances.getDesc();
      mDataSource = (RDFDataSource) instances.getDataSource();
      
      new RDFDataDescriptorEnhancer(mDataSource).fillDomain(mDataDesc);
      
      mNumOfClassLabels = mDataDesc.getTargetAttribute().getDomainSize();
      mClassEst = new ClassEstimator();
      mClassEst.estimateParameters(mDataSource, mDataDesc);
      List<RbcAttribute> nonTargetAttributes = discretizeNonTargetAttributes();
      List<RbcAttributeValue> nonTargetAttributeValues = propositionalizeAttributes(nonTargetAttributes);
      Log.warning(nonTargetAttributes.toString());
      buildClassifier(instances, nonTargetAttributeValues);
   }
   
   public void buildClassifier(LDInstances instances, List<RbcAttributeValue> nonTargetAttributeValues) throws Exception {
      Timer.INSTANCE.start("RDT learning");
      
      mDataDesc = (RDFDataDescriptor) instances.getDesc();
      mDataSource = (RDFDataSource) instances.getDataSource();
      if (mClassEst == null) {
         mClassEst = new ClassEstimator();
         mClassEst.estimateParameters(mDataSource, mDataDesc);
      }
      mNumOfClassLabels = mDataDesc.getTargetAttribute().getDomainSize();
      mAttributeValues = nonTargetAttributeValues;
      mTree = new SimpleDirectedGraph<TreeNodeSplitter, Boolean>(Boolean.class);
      buildTree(CollectionUtil.<TreeNodeSplitter>makeList(), CollectionUtil.<Boolean>makeList());
      
      List<TreeNodeSplitter> nodes = CollectionUtil.makeList(mTree.vertexSet()); 
      mTreeAttributeValues = CollectionUtil.makeList();
      for (TreeNodeSplitter n : nodes) {
         mTreeAttributeValues.add(n.getAttributeValue());
      }
      
      Log.warning("Final tree: " + mTree);
      
      Timer.INSTANCE.stop("RDT learning");
   }
   
   public SimpleDirectedGraph<TreeNodeSplitter, Boolean> getTree() {
      return mTree;
   }
   
   public double getTreeScore() {
      double accuracy = computeAccuracy();
      double sizePenalty = computeSizePenalty();
      Log.warning("accuracy=" + accuracy + " sizePenalty=" + sizePenalty);
      return accuracy * getTreeAttributes().size() - sizePenalty;
   }
   
   private double computeSizePenalty() {
      return 2.0 * mTree.vertexSet().size();
   }

   public double computeAccuracy() {
      return getTruePositiveCount(mRoot) / mClassEst.getNumInstances();
   }
   
   private double getTruePositiveCount(TreeNodeSplitter node) {
      double count = 0.0;
      for (int i = 0; i < 2; i++) {
         boolean exists = i == 0;
         TreeNodeSplitter next = findNode(node, exists);
         if (next == null) {
            count += node.getTruePositiveCount(exists);
         } else {
            count += getTruePositiveCount(next);
         }
      }
      return count;
   }
   
   private void buildTree(List<TreeNodeSplitter> pathNodes, List<Boolean> pathEdges) throws RDFDatabaseException {
      if (pathNodes.size() >= mDepthLimit) return;
      
      Log.info("Building tree: " + pathNodes + " " + pathEdges);
      Log.info("Current tree: " + mTree);
      
      List<RbcAttributeValue> unusedAttributeValues = getUnusedAttributeValues(pathNodes);
      List<TreeNodeSplitter> newNodes = CollectionUtil.makeList();
      for (RbcAttributeValue attValue : unusedAttributeValues) {
         TreeNodeSplitter newNode = new TreeNodeSplitter(mDataSource, mDataDesc, mClassEst, attValue);
         newNode.estimateParameters(pathNodes, pathEdges);
         newNodes.add(newNode);
      }
      TreeNodeSplitter lastNode = getLastNode(pathNodes);
      Boolean lastEdge = getLastEdge(pathEdges);
      TreeNodeSplitter bestNewNode = getMaxInfoGain(newNodes);
      
      Log.info("unusedAttributes=" + unusedAttributeValues);
      Log.info("newNodes=" + newNodes);
      
      if (mRoot == null || !isTerminationCriteriaMet(pathNodes, bestNewNode)) {
         mTree.addVertex(bestNewNode);
         pathNodes.add(bestNewNode);
         if (lastNode == null) {
            mRoot = bestNewNode;
         } else {
            if (lastEdge != null) {
               mTree.addEdge(lastNode, bestNewNode, lastEdge);
               pathEdges.add(lastEdge);
            }
         }
         
         for (int i = 0; i < 2; i++) {
            boolean exists = i == 0;
            List<TreeNodeSplitter> newPathNodes = CollectionUtil.makeList(pathNodes);
            List<Boolean> newPathEdges = CollectionUtil.makeList(pathEdges);
            newPathEdges.add(exists);
            buildTree(newPathNodes, newPathEdges);
         }
      }
   }
   
   private boolean isTerminationCriteriaMet(List<TreeNodeSplitter> pathNodes, TreeNodeSplitter bestNewNode) {
      if (bestNewNode == null) return true;
      Log.warning("" + bestNewNode.getInfoGain());
      return bestNewNode.getInfoGain() < INFO_GAIN_THRESHOLD;
   }

   private TreeNodeSplitter getLastNode(List<TreeNodeSplitter> pathNodes) {
      if (pathNodes.isEmpty()) return null;
      return pathNodes.get(pathNodes.size() - 1);
   }
   
   private Boolean getLastEdge(List<Boolean> pathEdges) {
      if (pathEdges.isEmpty()) return null;
      return pathEdges.get(pathEdges.size() - 1);
   }

   private TreeNodeSplitter getMaxInfoGain(List<TreeNodeSplitter> newNodes) {
      if (newNodes.isEmpty()) return null;
      
      TreeNodeSplitter maxNode = newNodes.get(0);
      double max = maxNode.getInfoGain();
      for (int i = 1; i < newNodes.size(); i++) {
         TreeNodeSplitter n = newNodes.get(i);
         if (n.getInfoGain() > max) {
            maxNode = n;
            max = n.getInfoGain();
         }
      }
      
      return maxNode;
   }

   private List<RbcAttributeValue> getUnusedAttributeValues(List<TreeNodeSplitter> pathNodes) {
      List<RbcAttributeValue> unused = CollectionUtil.makeList(mAttributeValues);
      for (TreeNodeSplitter n : pathNodes) {
         RbcAttributeValue v = n.getAttributeValue();
         unused.remove(v);
      }
      return unused;
   }

   private List<RbcAttribute> discretizeNonTargetAttributes() throws RDFDatabaseException {
      List<RbcAttribute> nonTargetAttributeList = mDataDesc.getNonTargetAttributeList();
      List<RbcAttribute> discretizedList = CollectionUtil.makeList();
      for (RbcAttribute att : nonTargetAttributeList) {
         AttributeEstimator estimator = att.getEstimator();
         if (estimator instanceof GaussianEstimator) {
            estimator.setDataSource(mDataSource, mDataDesc, mClassEst);
            estimator.estimateParameters();
            
            Log.warning(estimator.toString());
            RbcAttribute discretizedAtt = ((GaussianEstimator) estimator).makeBinaryBinnedAttribute();
            discretizedList.add(discretizedAtt);
         } else {
            discretizedList.add(att);
         }         
      }
      return discretizedList;
   }
   
   public static List<RbcAttributeValue> propositionalizeAttributes(List<RbcAttribute> atts) {
      List<RbcAttributeValue> attValues = CollectionUtil.makeList();
      for (RbcAttribute att : atts) {
         List<RbcAttributeValue> values = RbcAttributeValue.makeAllValues(att);
         attValues.addAll(values);
      }
      return attValues;
   }

   public List<RbcAttribute> getTreeAttributes() {
      List<RbcAttribute> atts = CollectionUtil.makeList();
      for (RbcAttributeValue v : mTreeAttributeValues) {
         atts.add(v.Attribute);
      }
      return atts;
   }
   
   @Override
   public double classifyInstance(LDInstance instance) throws Exception {
      return 0;
   }

   @Override
   public double[] distributionForInstance(LDInstance instance)
         throws Exception {
      return null;
   }

   public double classifyInstance(AggregatedInstance instance) {
      Map<RbcAttribute,AttributeValue> values = instance.getAttributeValues();
      TreeNodeSplitter currentNode = mRoot;
      while (true) {
         RbcAttributeValue attValue = currentNode.getAttributeValue();
         AttributeValue value = values.get(attValue.Attribute);
         MappedHistogram counts = (MappedHistogram) value;
         boolean exists = counts.get(attValue.ValueKey) > 0.0;
         TreeNodeSplitter nextNode = findNode(currentNode, exists);
         if (nextNode == null) {
            return currentNode.getPredictionAtLeaf(exists);
         } else {
            currentNode = nextNode;
         }
      }
   }
   
   private TreeNodeSplitter findNode(TreeNodeSplitter node, boolean exists) {
      for (Boolean out : mTree.outgoingEdgesOf(node)) {
         if (out == exists) {
            return mTree.getEdgeTarget(out);
         }
      }
      return null;
   }

   public double[] distributionForInstance(AggregatedInstance instance) {
      double[] dist = new double[mNumOfClassLabels];
      int label = (int) classifyInstance(instance);
      dist[label] = 1.0;
      return dist;
   }
   
   @Override
   public String toString() {
      return mTree.toString();
   }

}
