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
import airldm2.database.rdf.RDFDatabaseConnectionFactory;
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
      RDFDatabaseConnection conn = RDFDatabaseConnectionFactory.makeFromConfig();
      RDFDataSource dataSource = new RDFDataSource(conn, null, raw);
      TBox tBox = dataSource.getTBox();
      
      Repository repositoryRaw = new VirtuosoRepository("jdbc:virtuoso://localhost:1111/charset=UTF-8/log_enable=2", "dba", "dba", raw);
      repositoryRaw.initialize();
      ConnRaw = repositoryRaw.getConnection();
      
      Repository repositoryInf = new VirtuosoRepository("jdbc:virtuoso://localhost:1111/charset=UTF-8/log_enable=2", "dba", "dba", inferred);
      repositoryInf.initialize();
      ConnInf = repositoryInf.getConnection();
      
      RepositoryResult<Statement> rs = ConnRaw.getStatements(null, RDF.TYPE, null, false, ContextRaw);
      while (rs.hasNext()) {
         Statement stat = rs.next();
         Resource subject = stat.getSubject();
         URI object = (URI) stat.getObject();
         for (URI sup : tBox.getSuperclasses(object)) {
            ConnInf.add(subject, RDF.TYPE, sup, ContextInferred);
         }
      }
      
      ConnRaw.close();
      repositoryRaw.shutDown();
      ConnInf.close();
      repositoryInf.shutDown();
   }
      
}
