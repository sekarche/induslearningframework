package airldm2.database.rdf;

import java.util.List;

import org.openrdf.model.Value;

import airldm2.exceptions.RDFDatabaseException;


public interface RDFDatabaseConnection {

   List<Value[]> executeQuery(String query) throws RDFDatabaseException;
   
}
