package airldm2.classifiers.rl.ontology;

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
      return Graphs.predecessorListOf(mSubclass, c);
   }
   
   @Override
   public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
   }
   
}
