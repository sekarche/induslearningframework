package airldm2.database.rdf;

import airldm2.exceptions.RDFDatabaseException;


public interface RDFDatabaseConnection {

   SPARQLQueryResult executeQuery(String query) throws RDFDatabaseException;

   void executeUpdate(String query) throws RDFDatabaseException;
   
}
