package airldm2.core.rl;

import airldm2.core.ISufficentStatistic;
import airldm2.core.SSDataSource;
import airldm2.exceptions.RTConfigException;
import airldm2.util.AttribValuePair;

public class RDFDataSource implements SSDataSource {

   public RDFDataSource(String context) {
      // TODO Auto-generated constructor stub
   }

   @Override
   public void init(String config) throws RTConfigException {
      // TODO Auto-generated method stub
      
   }

   @Override
   public ISufficentStatistic getSufficientStatistic(String s) throws Exception {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public ISufficentStatistic[] getSufficientStatistic(String[] s)
         throws Exception {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public ISufficentStatistic getSufficientStatistic(AttribValuePair nameValue)
         throws Exception {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public ISufficentStatistic getSufficientStatistic(
         AttribValuePair[] nameValues) throws Exception {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public int getNumberInstances() throws Exception {
      // TODO Auto-generated method stub
      return 0;
   }

   @Override
   public void setRelationName(String relationName) {
      // TODO Auto-generated method stub
      
   }

}
