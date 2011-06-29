package airldm2.core.rl;

import java.util.List;

import org.openrdf.model.URI;

import airldm2.util.CollectionUtil;

public class PropertyChain {
   
   private List<URI> mChain;

   public PropertyChain() {
      mChain = CollectionUtil.makeList();
   }

   public PropertyChain(List<URI> props) {
      mChain = props;
   }
   
   public boolean isEmpty() {
      return mChain.isEmpty();
   }
   
   public URI get(int index) {
      return mChain.get(index);
   }
   
   public List<URI> getList() {
      return mChain;
   }
   
   public boolean contains(PropertyChain other) {
      for (URI o : other.mChain) {
         if (!mChain.contains(o)) return false;
      }
      return true;
   }

   public boolean hasURIStartsWith(URI[] uris) {
      if (uris == null) return false;
      
      for (URI uri : uris) {
         for (URI chain : mChain) {
            if (chain.stringValue().toLowerCase().startsWith(uri.stringValue().toLowerCase())) {
               return true;
            }
         }
      }
      return false;
   }
   
   public boolean containsDuplicate() {
      for (int i = 0; i < mChain.size() - 1; i++) {
         for (int j = i + 1; j < mChain.size() - 1; j++) {
            if (mChain.get(i).equals(mChain.get(j))) return true;
         }
      }
      return false;
   }

   @Override
   public boolean equals(Object other) {
      if (!(other instanceof PropertyChain)) return false;
      PropertyChain o = (PropertyChain) other;
      return mChain.equals(o.mChain);
   }
   
   @Override
   public String toString() {
      return mChain.toString();
   }

   public static PropertyChain make(PropertyChain propChain, URI prop) {
      List<URI> newChain = CollectionUtil.makeList();
      if (propChain != null) {
         newChain.addAll(propChain.mChain);
      }
      newChain.add(prop);
      return new PropertyChain(newChain);
   }

}