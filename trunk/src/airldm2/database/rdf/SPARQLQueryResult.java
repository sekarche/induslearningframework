package airldm2.database.rdf;

import java.util.List;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import airldm2.util.CollectionUtil;

public class SPARQLQueryResult {

   private List<Value[]> mResult;

   public SPARQLQueryResult(List<Value[]> result) {
      this.mResult = result;
   }
   
   public boolean isEmpty() {
      return mResult.isEmpty();
   }

   public int getInt() {
      Value[] rv = mResult.get(0);
      return ((Literal)rv[0]).intValue();
   }

   public double getDouble() {
      Value[] rv = mResult.get(0);
      return ((Literal)rv[0]).doubleValue();
   }

   public Value getValue() {
      if (mResult.isEmpty()) return null;
      Value[] rv = mResult.get(0);
      return rv[0];
   }

   public List<URI> getURIList() {
      List<URI> instances = CollectionUtil.makeList();
      for (Value[] rv : mResult) {
         if (rv[0] instanceof URI) {
            instances.add((URI) rv[0]);
         }
      }
      
      return instances;
   }
   
   public List<String> getStringList() {
      List<String> instances = CollectionUtil.makeList();
      for (Value[] rv : mResult) {
         instances.add(rv[0].stringValue());
      }
      
      return instances;
   }

   public List<Value> getValueList() {
      List<Value> instances = CollectionUtil.makeList();
      for (Value[] rv : mResult) {
         instances.add(rv[0]);
      }
      return instances;
   }

}
