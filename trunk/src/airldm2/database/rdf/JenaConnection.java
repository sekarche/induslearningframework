package airldm2.database.rdf;

import java.util.List;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

import airldm2.exceptions.RDFDatabaseException;
import airldm2.util.CollectionUtil;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class JenaConnection implements RDFDatabaseConnection {

   private String mSparqlEndpointURL;
   private ValueFactory mValueFac;

   public JenaConnection(String sparqlEndpointURL) {
      mSparqlEndpointURL = sparqlEndpointURL;
      mValueFac = new ValueFactoryImpl();
   }
   
   @Override
   public SPARQLQueryResult executeQuery(String query) throws RDFDatabaseException {
      List<Value[]> out = CollectionUtil.makeList();

      QueryExecution ex = QueryExecutionFactory.sparqlService(mSparqlEndpointURL, query);
      ResultSet results = ex.execSelect();
      List<String> vars = results.getResultVars();
      while (results.hasNext()) {
         QuerySolution result = results.next();
         Value[] values = new Value[vars.size()];
         for (int i = 0; i < values.length; i++) {
            values[i] = convertValue(result.get(vars.get(i)));
         }
         
         out.add(values);
      }

      return new SPARQLQueryResult(out);
   }
   
   private Value convertValue(com.hp.hpl.jena.rdf.model.RDFNode value) {
      if (value.isLiteral()) {
         return convertLiteral((com.hp.hpl.jena.rdf.model.Literal) value);
      }
      return convertResource((com.hp.hpl.jena.rdf.model.Resource) value);
   }

   private Literal convertLiteral(com.hp.hpl.jena.rdf.model.Literal literal) {
      return mValueFac.createLiteral(literal.getLexicalForm(), literal.getDatatypeURI() == null ? null : new URIImpl(literal.getDatatypeURI()));
   }

   private Resource convertResource(com.hp.hpl.jena.rdf.model.Resource resource) {
      if (resource.isAnon()) {
         return convertBNode(resource);
      }
      if (resource.isURIResource()) {
         return convertURI(resource);
      }
      throw new RuntimeException("Value could not be recognized");
   }
  
   private URI convertURI(com.hp.hpl.jena.rdf.model.Resource property) {
      return mValueFac.createURI(property.getURI().toString());
   }

   private BNode convertBNode(com.hp.hpl.jena.rdf.model.Resource resource) {
      return mValueFac.createBNode(resource.getId().getLabelString());
   }
  
}
