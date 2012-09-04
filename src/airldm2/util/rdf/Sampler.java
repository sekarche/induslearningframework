package airldm2.util.rdf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.Subgraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryException;

import airldm2.classifiers.rl.ontology.Cut;
import airldm2.classifiers.rl.ontology.TBox;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataDescriptorParser;
import airldm2.core.rl.RDFDataSource;
import airldm2.database.rdf.RDFDatabaseConnection;
import airldm2.database.rdf.SPARQLQueryResult;
import airldm2.database.rdf.VirtuosoConnection;
import airldm2.exceptions.RDFDataDescriptorFormatException;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.exceptions.RTConfigException;
import airldm2.util.CollectionUtil;
import explore.database.rdf.SubclassQueryConstructor;



public class Sampler {

   public static void main(String[] args) throws RepositoryException, RTConfigException, RDFDatabaseException, IOException, RDFDataDescriptorFormatException {
      //pkdd();
      //diseaseSample();
      //diseaseFix();
      //geneSample();
      //flickrSample();
      //flickrTBoxSample();
      //cutPrint("rdfs_example/flickrDescH.txt", ":flickr");
      //cutSample();
      //lastfmSample();
      //lastfmTBoxSample();
      //cutPrint("rdfs_example/lastfmDescH.txt", ":lastfm");
      
      flickrSubsetSample(1000);
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
   
   private static void geneSample() throws RDFDatabaseException, RepositoryException {
      RDFDatabaseConnection conn = new VirtuosoConnection("jdbc:virtuoso://localhost:1111/charset=UTF-8/log_enable=2", "dba", "dba");

      String query = "SELECT ?x FROM <:gene> WHERE { "
         + "?x a <http://kdd2002/vocab/gene> . "
         + "?x <http://kdd2002/vocab/hasLabel> \"-\" . } ORDER BY ?x";
      SPARQLQueryResult result = conn.executeQuery(query);
      List<URI> uriList = result.getURIList();
      Collections.shuffle(uriList, new Random(0));
      
      for (int i = uriList.size() - 1; i >= 344; i--) {
         URI uri = uriList.get(i);
         uriList.remove(i);
         
         String deleteQuery = "delete from <:gene> { ?x a <http://kdd2002/vocab/gene> . } WHERE { "
            + "?x a <http://kdd2002/vocab/gene> . "
            + "FILTER (?x = <" + uri + ">) }";
         conn.executeUpdate(deleteQuery);
      }
   }
   
   private static void flickrSample() throws RDFDatabaseException, RepositoryException {
      RDFDatabaseConnection conn = new VirtuosoConnection("jdbc:virtuoso://localhost:1113/charset=UTF-8/log_enable=2", "dba", "dba");
      String query;
      SPARQLQueryResult result;
      query = "select ?w { "
            + " FILTER(?c < 10) { "
            + " select ?w (count(distinct ?x) as ?c) { "
            + " ?x <http://flickr/vocab/hasPhoto> ?p . "
            + " ?p <http://flickr/vocab/hasTag> ?t . "
            + " ?t <http://flickr/vocab/hasSynset> ?s . "
            + " ?s a ?w . "
            + " } group by ?w "
            + " } }";
      
      result = conn.executeQuery(query);
      List<URI> wordList = result.getURIList();
      for (URI word : wordList) {
         String deleteQuery = "delete from <:flickr> { ?t <http://flickr/vocab/hasSynset> ?s . ?s a ?w . } WHERE { "
               + "?t <http://flickr/vocab/hasSynset> ?s . ?s a ?w . "
               + "FILTER (?w = <" + word + ">) }";
         conn.executeUpdate(deleteQuery);
      }
      
      query = "delete from <:flickr> { "
            + " ?p <http://flickr/vocab/hasTag> ?t . "
            + " } where { "
            + " ?p <http://flickr/vocab/hasTag> ?t . "
            + " FILTER NOT EXISTS { "
            + " ?t <http://flickr/vocab/hasSynset> ?s . "
            + " } }";
      conn.executeUpdate(query);
      
      query = "delete from <:flickr> { "
            + " ?x <http://flickr/vocab/hasPhoto> ?p . "
            + " } where { "
            + " ?x <http://flickr/vocab/hasPhoto> ?p . "
            + " FILTER NOT EXISTS { "
            + " ?p <http://flickr/vocab/hasTag> ?t . "
            + " } }";
      conn.executeUpdate(query);
      
      query = "select ?u (count(?p) as ?c) { "
            + "?u <http://flickr/vocab/hasPhoto> ?p . "
            + "} group by ?u";
      result = conn.executeQuery(query);
      List<Value[]> userList = result.getValueTupleList();

      for (Value[] user : userList) {
         URI uriUser = (URI) user[0];
         Literal count = (Literal) user[1];
         if (count.intValue() < 10) {
            String deleteQuery = "delete from <:flickr> { ?u a <http://flickr/vocab/user> . } WHERE { "
                  + "?u a <http://flickr/vocab/user> . "
                  + "FILTER (?u = <" + uriUser + ">) }";
            conn.executeUpdate(deleteQuery);
               
         } else if (count.intValue() > 50) {
            query = "select ?p { "
                  + "<" + uriUser + "> <http://flickr/vocab/hasPhoto> ?p . "
                  + "} order by ?p";
            result = conn.executeQuery(query);
            List<URI> photoList = result.getURIList();
            Collections.shuffle(photoList, new Random(0));
            
            for (int i = photoList.size() - 1; i >= 50; i--) {
               URI uri = photoList.get(i);
               photoList.remove(i);
               
               String deleteQuery = "delete from <:flickr> { ?u <http://flickr/vocab/hasPhoto> ?p . } WHERE { "
                  + "?u <http://flickr/vocab/hasPhoto> ?p . "
                  + "FILTER (?u = <" + uriUser + "> && ?p = <" + uri + ">) }";
               conn.executeUpdate(deleteQuery);
            }
         }
      }
   }
   
   private static void flickrTBoxSample() throws RDFDatabaseException, RepositoryException, FileNotFoundException {
      RDFDatabaseConnection conn = new VirtuosoConnection("jdbc:virtuoso://localhost:1113/charset=UTF-8/log_enable=2", "dba", "dba");

      TBox tBox = new TBox();
      
      //Convert from DAG to tree
      Set<URI> subclassSet = CollectionUtil.makeSet();
      
      String query = new SubclassQueryConstructor(":flickr").createQuery();
      
      SPARQLQueryResult results = conn.executeQuery(query);
      List<Value[]> valueTupleList = results.getValueTupleList();
      for (Value[] vs : valueTupleList) {
         if (vs[0] instanceof URI && vs[1] instanceof URI) {
            URI sub = (URI) vs[0];
            URI sup = (URI) vs[1];
            if (subclassSet.contains(sub)) continue;
            
            subclassSet.add(sub);
            tBox.addSubclass(sub, sup);
         } else {
            System.err.println(Arrays.toString(vs));
         }
      }
      
      tBox.computeClosure();
      SimpleDirectedGraph<URI, DefaultEdge> original = tBox.getOriginal();
      SimpleDirectedGraph<URI, DefaultEdge> closed = tBox.getClosed();
      
      query = "select  distinct ?w { " + 
            "?u a <http://flickr/vocab/user> . " + 
            "?u <http://flickr/vocab/hasPhoto> ?p . " +
            "?p <http://flickr/vocab/hasTag> ?t . " +
            "?t <http://flickr/vocab/hasSynset> ?s . " +
            "?s a ?w . " +
            "}";
      SPARQLQueryResult result = conn.executeQuery(query);
      List<URI> userList = result.getURIList();
      
      Set<URI> referred = CollectionUtil.makeSet();
      for (URI u : userList) {
         
         if (closed.containsVertex(u)) {
            List<URI> succ = Graphs.successorListOf(closed, u);
            referred.add(u);
            referred.addAll(succ);
         } else {
            System.out.println(u);
         }
      }
      
      Set<URI> all = CollectionUtil.makeSet(closed.vertexSet());
      
      all.removeAll(referred);
      for (URI remove : all) {
         original.removeVertex(remove);
      }
      
      PrintWriter out = new PrintWriter(new File("flickrSubclass.ttl")); 
      out.println("@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .");
      
      for (DefaultEdge edge : original.edgeSet()) {
         URI source = original.getEdgeSource(edge);
         URI target = original.getEdgeTarget(edge);
         out.println("<" + source + "> rdfs:subClassOf <" + target + "> .");
      }
      out.close();
   }
   
   private static void lastfmSample() throws RDFDatabaseException, RepositoryException {
      RDFDatabaseConnection conn = new VirtuosoConnection("jdbc:virtuoso://localhost:1113/charset=UTF-8/log_enable=2", "dba", "dba");
      String query;
      SPARQLQueryResult result;
      query = "select ?w { "
            + " FILTER(?c < 10) { "
            + " select ?w (count(distinct ?x) as ?c) { "
            + " ?x <http://lastfm/vocab/hasTrack> ?p . "
            + " ?p <http://lastfm/vocab/hasTag> ?t . "
            + " ?t <http://lastfm/vocab/hasSynset> ?s . "
            + " ?s a ?w . "
            + " } group by ?w "
            + " } }";
      
      result = conn.executeQuery(query);
      List<URI> wordList = result.getURIList();
      for (URI word : wordList) {
         String deleteQuery = "delete from <:lastfm> { ?t <http://lastfm/vocab/hasSynset> ?s . ?s a ?w . } WHERE { "
               + "?t <http://lastfm/vocab/hasSynset> ?s . ?s a ?w . "
               + "FILTER (?w = <" + word + ">) }";
         conn.executeUpdate(deleteQuery);
      }
      
      query = "delete from <:lastfm> { "
            + " ?p <http://lastfm/vocab/hasTag> ?t . "
            + " } where { "
            + " ?p <http://lastfm/vocab/hasTag> ?t . "
            + " FILTER NOT EXISTS { "
            + " ?t <http://lastfm/vocab/hasSynset> ?s . "
            + " } }";
      conn.executeUpdate(query);
      
      query = "delete from <:lastfm> { "
            + " ?x <http://lastfm/vocab/hasTrack> ?p . "
            + " } where { "
            + " ?x <http://lastfm/vocab/hasTrack> ?p . "
            + " FILTER NOT EXISTS { "
            + " ?p <http://lastfm/vocab/hasTag> ?t . "
            + " } }";
      conn.executeUpdate(query);
      
      query = "select ?u (count(?p) as ?c) { "
            + "?u <http://lastfm/vocab/hasTrack> ?p . "
            + "} group by ?u";
      result = conn.executeQuery(query);
      List<Value[]> userList = result.getValueTupleList();

      for (Value[] user : userList) {
         URI uriUser = (URI) user[0];
         Literal count = (Literal) user[1];
         if (count.intValue() < 10) {
            String deleteQuery = "delete from <:lastfm> { ?u a <http://lastfm/vocab/user> . } WHERE { "
                  + "?u a <http://lastfm/vocab/user> . "
                  + "FILTER (?u = <" + uriUser + ">) }";
            conn.executeUpdate(deleteQuery);
               
         } else if (count.intValue() > 50) {
            query = "select ?p { "
                  + "<" + uriUser + "> <http://lastfm/vocab/hasTrack> ?p . "
                  + "} order by ?p";
            result = conn.executeQuery(query);
            List<URI> photoList = result.getURIList();
            Collections.shuffle(photoList, new Random(0));
            
            for (int i = photoList.size() - 1; i >= 50; i--) {
               URI uri = photoList.get(i);
               photoList.remove(i);
               
               String deleteQuery = "delete from <:lastfm> { ?u <http://lastfm/vocab/hasTrack> ?p . } WHERE { "
                  + "?u <http://lastfm/vocab/hasTrack> ?p . "
                  + "FILTER (?u = <" + uriUser + "> && ?p = <" + uri + ">) }";
               conn.executeUpdate(deleteQuery);
            }
         }
      }
   }
   
   private static void lastfmTBoxSample() throws RDFDatabaseException, RepositoryException, FileNotFoundException {
      RDFDatabaseConnection conn = new VirtuosoConnection("jdbc:virtuoso://localhost:1113/charset=UTF-8/log_enable=2", "dba", "dba");

      TBox tBox = new TBox();
      
      //Convert from DAG to tree
      Set<URI> subclassSet = CollectionUtil.makeSet();
      
      String query = new SubclassQueryConstructor(":lastfm").createQuery();
      
      SPARQLQueryResult results = conn.executeQuery(query);
      List<Value[]> valueTupleList = results.getValueTupleList();
      for (Value[] vs : valueTupleList) {
         if (vs[0] instanceof URI && vs[1] instanceof URI) {
            URI sub = (URI) vs[0];
            URI sup = (URI) vs[1];
            if (subclassSet.contains(sub)) continue;
            
            subclassSet.add(sub);
            tBox.addSubclass(sub, sup);
         } else {
            System.err.println(Arrays.toString(vs));
         }
      }
      
      tBox.computeClosure();
      SimpleDirectedGraph<URI, DefaultEdge> original = tBox.getOriginal();
      SimpleDirectedGraph<URI, DefaultEdge> closed = tBox.getClosed();
      
      query = "select  distinct ?w { " + 
            "?u a <http://lastfm/vocab/user> . " + 
            "?u <http://lastfm/vocab/hasTrack> ?p . " +
            "?p <http://lastfm/vocab/hasTag> ?t . " +
            "?t <http://lastfm/vocab/hasSynset> ?s . " +
            "?s a ?w . " +
            "}";
      SPARQLQueryResult result = conn.executeQuery(query);
      List<URI> userList = result.getURIList();
      
      Set<URI> referred = CollectionUtil.makeSet();
      for (URI u : userList) {
         
         if (closed.containsVertex(u)) {
            List<URI> succ = Graphs.successorListOf(closed, u);
            referred.add(u);
            referred.addAll(succ);
         } else {
            System.out.println(u);
         }
      }
      
      Set<URI> all = CollectionUtil.makeSet(closed.vertexSet());
      
      all.removeAll(referred);
      for (URI remove : all) {
         original.removeVertex(remove);
      }
      
      PrintWriter out = new PrintWriter(new File("lastfmSubclass.ttl")); 
      out.println("@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .");
      
      for (DefaultEdge edge : original.edgeSet()) {
         URI source = original.getEdgeSource(edge);
         URI target = original.getEdgeTarget(edge);
         out.println("<" + source + "> rdfs:subClassOf <" + target + "> .");
      }
      out.close();
   }
   
   private static void cutPrint(String descFile, String context) throws RDFDatabaseException, RepositoryException, IOException, RDFDataDescriptorFormatException {
      VirtuosoConnection conn = new VirtuosoConnection("jdbc:virtuoso://localhost:1113/charset=UTF-8/log_enable=2", "dba", "dba");
      RDFDataDescriptor desc = RDFDataDescriptorParser.parse(descFile);
      RDFDataSource source = new RDFDataSource(conn, desc, context);
      TBox tBox = source.getTBox();
      URI hierarchyRoot = desc.getNonTargetAttributeList().get(0).getHierarchyRoot();
      Cut cut = tBox.getRootCut(hierarchyRoot);
      
      BufferedWriter out = new BufferedWriter(new FileWriter("cut.txt"));
      
      int lastSize = cut.size();
      while (cut.refineGreedyBFS()) {
         if (cut.size() > lastSize) {
            lastSize = cut.size();
            out.write(cut.get().toString());
            out.newLine();
            System.out.println(cut.size());
         }
      }
      out.close();
      
   }

   private static void cutSample() throws IOException {
      final int TOTAL = 50;
      final int MAX = 13416;
      final double LOG_STEP = Math.log10(MAX) / TOTAL;
      
      int[] cutpoints = new int[TOTAL + 1];
      for (int i = 1; i < cutpoints.length; i++) {
         cutpoints[i] = (int) Math.pow(10, LOG_STEP * i);
         System.out.println(cutpoints[i]);
      }
      cutpoints[0] = 1;
      cutpoints[cutpoints.length - 1] = MAX;
      
      BufferedWriter out = new BufferedWriter(new FileWriter("cutFiltered.txt"));
      BufferedReader in = new BufferedReader(new FileReader("cut.txt"));
      
      String line;
      int nextCutpoint = 0;
      while ((line=in.readLine()) != null) {
         int size = line.split(",").length;
         if (size >= cutpoints[nextCutpoint]) {
            nextCutpoint++;
            out.write(line);
            out.newLine();
         }
      }
      in.close();
      out.close();
   }

   
   private static void flickrSubsetSample(int size) throws RDFDatabaseException, RepositoryException {
      RDFDatabaseConnection conn = new VirtuosoConnection("jdbc:virtuoso://localhost:1113/charset=UTF-8/log_enable=2", "dba", "dba");
      String query;
      
      query = "select ?x from <:flickr> { "
            + " ?x a <http://flickr/vocab/user> . "
            + " ?x <http://flickr/vocab/hasGroup> \"AbandonedCalifornia\" . "
            + " } order by ?x limit " + (size / 2);
      
      sample(conn, query);
      
      query = "select ?x from <:flickr> { "
            + " ?x a <http://flickr/vocab/user> . "
            + " ?x <http://flickr/vocab/hasGroup> \"FindingHome\" . "
            + " } order by ?x limit " + (size / 2);
      
      sample(conn, query);
   }

   private static void sample(RDFDatabaseConnection conn, String query)
         throws RDFDatabaseException {
      SPARQLQueryResult result;
      result = conn.executeQuery(query);
      List<URI> userList = result.getURIList();
      for (URI user : userList) {
         String graph = "<" + user + "> a <http://flickr/vocab/user> . "
               + "<" + user + "> <http://flickr/vocab/hasGroup> ?g . "
               + "<" + user + "> <http://flickr/vocab/hasPhoto> ?p . "
               + "?p <http://flickr/vocab/hasTag> ?t . "
               + "?t <http://flickr/vocab/hasSynset> ?s . "
               + "?s a ?a . ";
         String insertQuery = "insert into <:subset> { "
               + graph
               + " } WHERE { "
               + graph
               + " }";
         System.out.println(insertQuery);
         conn.executeUpdate(insertQuery);
      }
   }
   
}
