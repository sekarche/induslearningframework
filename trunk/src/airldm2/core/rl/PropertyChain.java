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
   
   public PropertyChain append(URI prop) {
      List<URI> newChain = CollectionUtil.makeList(mChain);
      newChain.add(prop);
      return new PropertyChain(newChain);
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
   
}