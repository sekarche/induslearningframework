package airldm2.classifiers.rl.ontology;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jgrapht.Graphs;
import org.jgrapht.alg.KruskalMinimumSpanningTree;
import org.jgrapht.alg.TransitiveClosure;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.openrdf.model.URI;

import airldm2.util.CollectionUtil;

public class TBox {

   private SimpleDirectedGraph<URI,DefaultEdge> mSubclass;
   private SimpleDirectedGraph<URI,DefaultEdge> mSubclassClosed;
   
   public TBox() {
      mSubclass = new SimpleDirectedGraph<URI,DefaultEdge>(DefaultEdge.class);
   }
   
   public void addSubclass(URI sub, URI sup) {
      mSubclass.addVertex(sub);
      mSubclass.addVertex(sup);
      mSubclass.addEdge(sub, sup);
   }
   
   public void computeClosure() {
      TransitiveClosure.INSTANCE.closeSimpleDirectedGraph(mSubclass);
      mSubclassClosed = new SimpleDirectedGraph<URI,DefaultEdge>(DefaultEdge.class);
      Graphs.addGraph(mSubclassClosed, mSubclass);
      
      KruskalMinimumSpanningTree<URI,DefaultEdge> mst = new KruskalMinimumSpanningTree<URI,DefaultEdge>(mSubclass);
      Set<DefaultEdge> mstEdges = mst.getEdgeSet();
      Set<DefaultEdge> allEdges = CollectionUtil.makeSet(mSubclass.edgeSet());
      for (DefaultEdge edge : allEdges) {
         if (!mstEdges.contains(edge)) {
            mSubclass.removeEdge(edge);
         }
      }
   }
   
   public Cut getRootCut(URI root) {
      return new Cut(this, root);
   }
   
   public List<URI> getDirectSubclass(URI c) {
      if (!mSubclass.containsVertex(c)) return Collections.emptyList();
      
      return Graphs.predecessorListOf(mSubclass, c);
   }
   
   public URI getDirectSuperclass(URI c) {
      if (!mSubclass.containsVertex(c)) return null;
      
      List<URI> successors = Graphs.successorListOf(mSubclass, c);
      return successors.get(0);
   }
   
   public List<URI> getSiblings(URI c) {
      return getDirectSubclass(getDirectSuperclass(c));
   }
   
   public List<URI> getSuperclasses(URI c) {
      if (!mSubclassClosed.containsVertex(c)) return Collections.emptyList();
      
      return Graphs.successorListOf(mSubclassClosed, c);
   }
   
   public Cut getLeafCut(URI root) {
      List<URI> leaf = CollectionUtil.makeList();
      for (URI pred : Graphs.predecessorListOf(mSubclassClosed, root)) {
         if (mSubclassClosed.inDegreeOf(pred) == 0) {
            leaf.add(pred);
         }
      }
      return new Cut(this, leaf);
   }
   
   public List<URI> getLeaves() {
      List<URI> leaves = CollectionUtil.makeList();
      for (URI uri : mSubclassClosed.vertexSet()) {
         if (mSubclassClosed.inDegreeOf(uri) == 0) {
            leaves.add(uri);
         }
      }
      return leaves;
   }
   
   @Override
   public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
   }

}
