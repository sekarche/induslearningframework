package airldm2.util.rdf;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import virtuoso.sesame2.driver.VirtuosoRepository;
import airldm2.classifiers.rl.ontology.TBox;
import airldm2.core.rl.RDFDataSource;
import airldm2.database.rdf.RDFDatabaseConnection;
import airldm2.database.rdf.VirtuosoConnection;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.exceptions.RTConfigException;

public class SubclassReasoner {

   private static RepositoryConnection ConnRaw;
   private static RepositoryConnection ConnInf;
   private static ValueFactory ValueFac = new ValueFactoryImpl();
   private static URI ContextRaw;
   private static URI ContextInferred;
   
   public static void main(String[] args) throws RepositoryException, RTConfigException, RDFDatabaseException {
      String raw = args[0];
      String inferred = args[1];
      ContextRaw = ValueFac.createURI(raw);
      ContextInferred = ValueFac.createURI(inferred);
      RDFDatabaseConnection conn = new VirtuosoConnection("http://localhost:8890/sparql");
      RDFDataSource dataSource = new RDFDataSource(conn, null, raw);
      
      Timer timer = new Timer();
      timer.start("TBox");
      TBox tBox = dataSource.getTBox();
      timer.stop();
      
      Repository repositoryRaw = new VirtuosoRepository("jdbc:virtuoso://localhost:1111/charset=UTF-8", "dba", "dba", raw);
      repositoryRaw.initialize();
      ConnRaw = repositoryRaw.getConnection();
      
      Repository repositoryInf = new VirtuosoRepository("jdbc:virtuoso://localhost:1111/charset=UTF-8", "dba", "dba", inferred);
      repositoryInf.initialize();
      ConnInf = repositoryInf.getConnection();
      ConnInf.setAutoCommit(false);
      
      timer.start("Inference");
      RepositoryResult<Statement> rs = ConnRaw.getStatements(null, RDF.TYPE, null, false, ContextRaw);
      int i = 0;
      while (rs.hasNext()) {
         Statement stat = rs.next();
         Resource subject = stat.getSubject();
         URI object = (URI) stat.getObject();
         
         for (URI sup : tBox.getSuperclasses(object)) {
            ConnInf.add(subject, RDF.TYPE, sup, ContextInferred);
         }
         
         if (++i % 10000 == 0) {
            System.out.println(i);
            ConnInf.commit();
         }
      }
      ConnInf.commit();
      
      timer.stop();
      
      ConnRaw.close();
      repositoryRaw.shutDown();
      ConnInf.close();
      repositoryInf.shutDown();
   }
}

class Timer {
   
   private String mName;
   private long mTime;
   
   public void start(String name) {
      mName = name;
      mTime = System.currentTimeMillis();
   }
   
   public void stop() {
      System.out.println(mName + ": " + (System.currentTimeMillis() - mTime) / 1000.0);
   }
   
}