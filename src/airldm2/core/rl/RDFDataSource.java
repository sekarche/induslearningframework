package airldm2.core.rl;

import org.openrdf.repository.Repository;

import virtuoso.sesame2.driver.VirtuosoRepository;
import airldm2.core.ISufficentStatistic;
import airldm2.core.SSDataSource;
import airldm2.exceptions.RTConfigException;
import airldm2.util.AttribValuePair;

public class RDFDataSource implements SSDataSource {

   private Repository mRepository;

   public RDFDataSource(String context) {
      mRepository = new VirtuosoRepository("", "dba", "dba");
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

}
