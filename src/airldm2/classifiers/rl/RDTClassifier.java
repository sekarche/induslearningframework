package airldm2.classifiers.rl;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jgrapht.graph.SimpleDirectedGraph;

import airldm2.classifiers.Classifier;
import airldm2.classifiers.rl.estimator.AttributeEstimator;
import airldm2.classifiers.rl.estimator.AttributeValue;
import airldm2.classifiers.rl.estimator.Category;
import airldm2.classifiers.rl.estimator.ClassEstimator;
import airldm2.classifiers.rl.estimator.GaussianEstimator;
import airldm2.classifiers.rl.tree.TreeNodeSplitter;
import airldm2.core.LDInstance;
import airldm2.core.LDInstances;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.util.CollectionUtil;
import airldm2.util.Timer;


public class RDTClassifier extends Classifier {

   protected static Logger Log = Logger.getLogger("airldm2.classifiers.rl.RDTClassifier");
   static { Log.setLevel(Level.WARNING); }
   
   private static final double INFO_GAIN_THRESHOLD = 0.1;
   
   private RDFDataSource mDataSource;
   private RDFDataDescriptor mDataDesc;
   private ClassEstimator mClassEst;
   
   private int mNumOfClassLabels;
   private List<RbcAttribute> mNonTargetAttributes;
   private SimpleDirectedGraph<TreeNodeSplitter,Category> mTree;
   private TreeNodeSplitter mRoot;
   
   private List<RbcAttribute> mTreeAttributes;

   private int mDepthLimit = Integer.MAX_VALUE;
   
   public RDTClassifier() {
   }
   
   public RDTClassifier(int depthLimit) {
      mDepthLimit = depthLimit;
   }
   
   public void buildClassifier(LDInstances instances, List<RbcAttribute> nonTargetAttributes) throws Exception {
      Timer.INSTANCE.start("RDT learning");
      
      mDataDesc = (RDFDataDescriptor) instances.getDesc();
      mDataSource = (RDFDataSource) instances.getDataSource();
      if (mClassEst == null) {
         mClassEst = new ClassEstimator();
         mClassEst.estimateParameters(mDataSource, mDataDesc);
      }
      mNumOfClassLabels = mDataDesc.getTargetAttribute().getDomainSize();
      mNonTargetAttributes = nonTargetAttributes;
      mTree = new SimpleDirectedGraph<TreeNodeSplitter, Category>(Category.class);
      buildTree(CollectionUtil.<TreeNodeSplitter>makeList(), CollectionUtil.<Category>makeList());
      
      List<TreeNodeSplitter> nodes = CollectionUtil.makeList(mTree.vertexSet()); 
      mTreeAttributes = CollectionUtil.makeList();
      for (TreeNodeSplitter n : nodes) {
         mTreeAttributes.add(n.getAttribute());
      }
      
      Log.warning("Final tree: " + mTree);
      
      Timer.INSTANCE.stop("RDT learning");
   }
   
   public SimpleDirectedGraph<TreeNodeSplitter, Category> getTree() {
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

   private double computeAccuracy() {
      return getTruePositiveCount(mRoot) / mClassEst.getNumInstances();
   }
   
   private double getTruePositiveCount(TreeNodeSplitter node) {
      double count = 0.0;
      for (int i = 0; i < 2; i++) {
         Category cat = new Category(i);
         TreeNodeSplitter next = findNode(node, cat);
         if (next == null) {
            count += node.getTruePositiveCount(cat);
         } else {
            count += getTruePositiveCount(next);
         }
      }
      return count;
   }
   
   @Override
   public void buildClassifier(LDInstances instances) throws Exception {
      mDataDesc = (RDFDataDescriptor) instances.getDesc();
      mDataSource = (RDFDataSource) instances.getDataSource();
      mNumOfClassLabels = mDataDesc.getTargetAttribute().getDomainSize();
      mClassEst = new ClassEstimator();
      mClassEst.estimateParameters(mDataSource, mDataDesc);
      List<RbcAttribute> nonTargetAttributes = discretizeNonTargetAttributes();
      Log.warning(nonTargetAttributes.toString());
      buildClassifier(instances, nonTargetAttributes);
   }
   
   private void buildTree(List<TreeNodeSplitter> pathNodes, List<Category> pathEdges) throws RDFDatabaseException {
      if (pathNodes.size() >= mDepthLimit) return;
      
      Log.warning("Building tree: " + pathNodes + " " + pathEdges);
      Log.warning("Current tree: " + mTree);
      
      List<RbcAttribute> unusedAttributes = getUnusedAttributes(pathNodes);
      List<TreeNodeSplitter> newNodes = CollectionUtil.makeList();
      for (RbcAttribute att : unusedAttributes) {
         TreeNodeSplitter newNode = new TreeNodeSplitter(mDataSource, mDataDesc, att);
         newNode.estimateParameters(pathNodes, pathEdges);
         newNodes.add(newNode);
      }
      TreeNodeSplitter lastNode = getLastNode(pathNodes);
      Category lastEdge = getLastEdge(pathEdges);
      TreeNodeSplitter bestNewNode = getMaxInfoGain(newNodes);
      
      Log.warning("unusedAttributes=" + unusedAttributes);
      Log.warning("newNodes=" + newNodes);
      
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
         
         for (int i = 0; i <= 1; i++) {
            List<TreeNodeSplitter> newPathNodes = CollectionUtil.makeList(pathNodes);
            List<Category> newPathEdges = CollectionUtil.makeList(pathEdges);
            newPathEdges.add(new Category(i));
            buildTree(newPathNodes, newPathEdges);
         }
      }
   }
   
   private boolean isTerminationCriteriaMet(List<TreeNodeSplitter> pathNodes, TreeNodeSplitter bestNewNode) {
      if (bestNewNode == null) return true;
      
      Log.warning(bestNewNode.toString() + " " + bestNewNode.getInfoGain());
      return bestNewNode.getInfoGain() < INFO_GAIN_THRESHOLD;
   }

   private TreeNodeSplitter getLastNode(List<TreeNodeSplitter> pathNodes) {
      if (pathNodes.isEmpty()) return null;
      return pathNodes.get(pathNodes.size() - 1);
   }
   
   private Category getLastEdge(List<Category> pathEdges) {
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

   private List<RbcAttribute> getUnusedAttributes(List<TreeNodeSplitter> pathNodes) {
      List<RbcAttribute> unused = CollectionUtil.makeList(mNonTargetAttributes);
      for (TreeNodeSplitter n : pathNodes) {
         RbcAttribute att = n.getAttribute();
         unused.remove(att);
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
   
   public List<RbcAttribute> getTreeAttributes() {
      return mTreeAttributes;
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
         RbcAttribute att = currentNode.getAttribute();
         AttributeValue value = values.get(att);
         Category cat = (Category) value;
         TreeNodeSplitter nextNode = findNode(currentNode, cat);
         if (nextNode == null) {
            return currentNode.getPredictionAtLeaf(cat);
         } else {
            currentNode = nextNode;
         }
      }
   }
   
   private TreeNodeSplitter findNode(TreeNodeSplitter node, Category cat) {
      for (Category out : mTree.outgoingEdgesOf(node)) {
         if (out.getIndex() == cat.getIndex()) {
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
