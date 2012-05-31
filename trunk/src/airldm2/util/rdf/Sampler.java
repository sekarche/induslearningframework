package airldm2.util.rdf;

import java.util.List;
import java.util.Random;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.Subgraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryException;

import airldm2.database.rdf.RDFDatabaseConnection;
import airldm2.database.rdf.SPARQLQueryResult;
import airldm2.database.rdf.VirtuosoConnection;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.exceptions.RTConfigException;
import airldm2.util.CollectionUtil;


public class Sampler {

   public static void main(String[] args) throws RepositoryException, RTConfigException, RDFDatabaseException {
      //pkdd();
      diseaseSample();
      //diseaseFix();
   }

   private static void diseaseFix() throws RDFDatabaseException, RepositoryException {
      RDFDatabaseConnection conn = new VirtuosoConnection("jdbc:virtuoso://localhost:1111/charset=UTF-8/log_enable=2", "dba", "dba");

      String query = "SELECT ?x ?d FROM <http://ehr> WHERE { "
         + "?x <http://ehr/data/vocab/has_disease> ?d . }";
      SPARQLQueryResult result = conn.executeQuery(query);
      List<Value[]> pairList = result.getValueTupleList();

      for (Value[] v : pairList) {
         //System.out.println(Arrays.toString(v));
         URI d = (URI) v[1];
         int slash = d.toString().lastIndexOf("/");
         String ID = d.toString().substring(slash + 1);
         
         String insertQuery = "INSERT INTO <http://ehr> { "
            + "<" + v[0].toString() + "> <http://ehr/data/vocab/has_disease> <http://ehr/data/vocab/disease/" + ID + "> . "
            + "<http://ehr/data/vocab/disease/" + ID + "> a <" + d + "> . }";
         conn.executeUpdate(insertQuery);

         String deleteQuery = "DELETE FROM <http://ehr> { "
            + "<" + v[0].toString() + "> <http://ehr/data/vocab/has_disease> <" + d + "> . }";
         conn.executeUpdate(deleteQuery);
      }
   }

   private static void diseaseSample() throws RDFDatabaseException, RepositoryException {
      RDFDatabaseConnection conn = new VirtuosoConnection("jdbc:virtuoso://localhost:1111/charset=UTF-8/log_enable=2", "dba", "dba");

      String query = "SELECT ?d1 ?d2 FROM <http://ehr> WHERE { "
         + "?x a <http://ehr/data/vocab/disease_pair> . "
         + "?x <http://ehr/data/vocab/has_disease> ?d1 . "
         + "?x <http://ehr/data/vocab/has_disease> ?d2 . "
         + "FILTER (?d1 != ?d2) }";
      SPARQLQueryResult result = conn.executeQuery(query);
      List<Value[]> pairList = result.getValueTupleList();
      SimpleGraph<URI,DefaultEdge> graph = parseGraph(pairList);
      
//      String deleteQuery = "delete from <http://ehr> {?x ?y ?z . } WHERE { "
//         + "?x ?y ?z . "
//         + "?x a <http://ehr/data/vocab/disease_pair> . " 
//         + "?x <http://ehr/data/vocab/relative_risk> ?r . "
//         + "FILTER (?r < 20) }";
//      conn.executeUpdate(deleteQuery);
//      
//      result = conn.executeQuery(query);
//      pairList = result.getValueTupleList();
//      SimpleGraph<URI,DefaultEdge> graph20 = parseGraph(pairList);
      
      Subgraph<URI, DefaultEdge, SimpleGraph<URI, DefaultEdge>> sampled = sampleGraph(graph, 100);
      
      for (DefaultEdge e : graph.edgeSet()) {
         if (sampled.containsEdge(e)) continue;
         
         URI d1 = graph.getEdgeSource(e);
         URI d2 = graph.getEdgeTarget(e);
         
         String deleteQuery = "DELETE FROM <http://ehr> { ?x ?y ?z . } WHERE { "
            + "?x <http://ehr/data/vocab/has_disease> <" + d1 + "> . "
            + "?x <http://ehr/data/vocab/has_disease> <" + d2 + "> . "
            + "?x ?y ?z . }";
         conn.executeUpdate(deleteQuery);
      }
      
      for (URI v : sampled.vertexSet()) {
         int degree = sampled.edgesOf(v).size();
         String insertQuery = "INSERT INTO <http://ehr> { "
            + "<" + v + "> <http://ehr/data/vocab/has_degree> " + degree + " . } ";
         conn.executeUpdate(insertQuery);
      }
      
      int posEdges = sampled.edgeSet().size();
      int negEdges = 1;
      List<URI> diseases = CollectionUtil.makeList(graph.vertexSet());
      Random r = new Random();
      while (negEdges <= posEdges) {
         int d1 = r.nextInt(diseases.size());
         int d2 = r.nextInt(diseases.size());
         URI u1 = diseases.get(d1);
         URI u2 = diseases.get(d2);
         if (d1 == d2 || !sampled.containsVertex(u1) || !sampled.containsVertex(u2) || graph.containsEdge(u1, u2)) continue;
         
         if (d2 < d1) {
            int tmp = d2;
            d2 = d1;
            d1 = tmp;
         }
         
         String insertQuery = "INSERT INTO <http://ehr> { "
            + "<http://ehr/stat/disease_pair/n" + negEdges + "> a <http://ehr/data/vocab/disease_pair> ; "
            + "<http://ehr/data/vocab/has_disease> <" + u1 + "> ; "
            + "<http://ehr/data/vocab/has_disease> <" + u2 + "> ; "
            + "<http://ehr/data/vocab/relative_risk> 0 ."
            + "}"; 
         conn.executeUpdate(insertQuery);
         
         negEdges++;
      }
   }

   private static Subgraph<URI, DefaultEdge, SimpleGraph<URI, DefaultEdge>> sampleGraph(SimpleGraph<URI, DefaultEdge> graph, int nodeSize) {
      SimpleGraph<URI, DefaultEdge> sampledGraph = new SimpleGraph<URI, DefaultEdge>(DefaultEdge.class);
      Graphs.addGraph(sampledGraph, graph);
      
      boolean hasChanged = true;
      while (hasChanged) {
         hasChanged = false;
         Set<URI> vertexSet = CollectionUtil.makeSet(sampledGraph.vertexSet());
         for (URI v : vertexSet) {
            if (sampledGraph.degreeOf(v) <= 1) {
               sampledGraph.removeVertex(v);
               hasChanged = true;
            }
         }
      }
      
      BreadthFirstIterator<URI, DefaultEdge> it = new BreadthFirstIterator<URI, DefaultEdge>(sampledGraph);
      Set<URI> ns = CollectionUtil.makeSet();
      for (; it.hasNext() && ns.size() < nodeSize; ) {
         URI n = it.next();
         ns.add(n);
      }
      
      return new Subgraph<URI, DefaultEdge, SimpleGraph<URI, DefaultEdge>>(graph, ns);
   }

   private static SimpleGraph<URI, DefaultEdge> parseGraph(List<Value[]> pairList) {
      SimpleGraph<URI,DefaultEdge> graph = new SimpleGraph<URI,DefaultEdge>(DefaultEdge.class);
      for (Value[] v : pairList) {
         URI d1 = (URI) v[0];
         URI d2 = (URI) v[1];
         graph.addVertex(d1);
         graph.addVertex(d2);
         graph.addEdge(d1, d2);
      }
      return graph;
   }

   private static void pkdd() throws RepositoryException, RDFDatabaseException {
      RDFDatabaseConnection conn = new VirtuosoConnection("jdbc:virtuoso://localhost:1111/charset=UTF-8/log_enable=2", "dba", "dba");

      String query = "SELECT ?x FROM <:financial> WHERE { ?x <http://lisp.vse.cz/pkdd99/vocab/resource/loan_status> \"A\" . }";
      SPARQLQueryResult result = conn.executeQuery(query);
      List<URI> uriList = result.getURIList();
      List<URI> toRemove = CollectionUtil.makeList();
      
      Random r = new Random();
      while (uriList.size() > 324) {
         int nextInt = r.nextInt(uriList.size());
         URI removed = uriList.remove(nextInt);
         toRemove.add(removed);
      }
      
      for (URI i : toRemove) {
         StringBuilder deleteQuery = new StringBuilder();
         deleteQuery.append("DELETE FROM <:financial> { <" + i.toString() + "> ?y ?z } WHERE { <" + i.toString() + "> ?y ?z . }");
     
         conn.executeUpdate(deleteQuery.toString());
      }
   }
}
