package airldm2.core.rl;

import static airldm2.util.StringUtil.triple;

import java.util.List;

import org.openrdf.model.URI;

import airldm2.classifiers.rl.ontology.TBox;
import airldm2.database.rdf.RDFDatabaseConnectionFactory;

public class OntologyEnumType extends EnumType {

   public static String NAME = "OntologyENUM";
   
   private TBox mTBox;

   public OntologyEnumType(TBox tBox, List<URI> domain) {
      super(domain);
      mTBox = tBox;
   }

   @Override
   public String makeFilter(String varName, int valueIndex) {
      String filter = super.makeFilter(varName, valueIndex);
      
      URI v = mDomain.get(valueIndex);
      if (RDFDatabaseConnectionFactory.QUERY_INFERENCE && !mTBox.isLeaf(v)) {
         String transVar = "?transitive";
         filter = 
            "{ SELECT * WHERE { "
            + triple(varName, "rdfs:subClassOf", transVar)
            + " } } "
            + "OPTION(TRANSITIVE, t_distinct, t_in(" + varName + "), t_out(" + transVar + ")) . "
            + "FILTER(" + transVar + " = <" + v + ">) ";
      }
      
      return filter;
   }

}
