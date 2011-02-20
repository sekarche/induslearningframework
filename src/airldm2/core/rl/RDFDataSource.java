package airldm2.core.rl;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import virtuoso.sesame2.driver.VirtuosoRepository;
import airldm2.constants.Constants;
import airldm2.core.ISufficentStatistic;
import airldm2.core.SSDataSource;
import airldm2.database.rdf.SuffStatQueryConstructor;
import airldm2.database.rdf.SuffStatQueryParameter;
import airldm2.exceptions.RTConfigException;
import airldm2.util.AttribValuePair;

public class RDFDataSource implements SSDataSource {

   private Repository mRepository;
   private RepositoryConnection mConn;
   private String mDefaultContext;

   public RDFDataSource(String context) throws RepositoryException, RTConfigException {
      mDefaultContext = context;
      
      final Properties defaultProps = new Properties();
      try {
         FileInputStream in = new FileInputStream(Constants.RDFSTORE_PROPERTIES_RESOURCE_PATH);
         defaultProps.load(in);
         in.close();
      } catch (IOException e) {
         throw new RTConfigException("Error reading " + Constants.RDFSTORE_PROPERTIES_RESOURCE_PATH, e);
      }

      String url = defaultProps.getProperty("DataSource.url");
      if (url != null) url = url.trim();
      String username = defaultProps.getProperty("DataSource.username");
      String password = defaultProps.getProperty("DataSource.password");

      mRepository = new VirtuosoRepository(url, username, password);
      mConn = mRepository.getConnection();
   }

   @Override
   public void init(String config) throws RTConfigException {
   }

   @Override
   public ISufficentStatistic getSufficientStatistic(String s) throws Exception {
      return null;
   }

   @Override
   public ISufficentStatistic[] getSufficientStatistic(String[] s)
         throws Exception {
      return null;
   }

   @Override
   public ISufficentStatistic getSufficientStatistic(AttribValuePair nameValue)
         throws Exception {
      return null;
   }

   @Override
   public ISufficentStatistic getSufficientStatistic(
         AttribValuePair[] nameValues) throws Exception {
      return null;
   }

   @Override
   public int getNumberInstances() throws Exception {
      return 0;
   }

   @Override
   public void setRelationName(String relationName) {
   }

   public ISufficentStatistic getSufficientStatistic(SuffStatQueryParameter queryParam) {
      String query = new SuffStatQueryConstructor(mDefaultContext, queryParam).createQuery();
      return null;
   }

}
