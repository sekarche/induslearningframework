package airldm2.classifiers.rl;

import java.util.List;

import org.openrdf.model.URI;

import airldm2.util.CollectionUtil;

public class AggregatedInstances {

   private final List<URI> mURIs;
   private List<AggregatedInstance> mInstances;

   public AggregatedInstances(List<URI> instanceURIs) {
      mURIs = instanceURIs;
      mInstances = CollectionUtil.makeList();
   }

   public void setInstances(List<AggregatedInstance> aggInstances) {
      mInstances = aggInstances;
   }
   
   public List<URI> getURIs() {
      return mURIs;
   }
   
   public List<AggregatedInstance> getInstances() {
      return mInstances;
   }

   public void addAttribute(List<ValueIndexCount> indexCounts) {
      for (int i = 0; i < mInstances.size(); i++) {
         AggregatedInstance instance = mInstances.get(i);
         ValueIndexCount indexCount = indexCounts.get(i);
         instance.addAttribute(indexCount);
      }
   }
   
   public void removeLastAttribute() {
      for (AggregatedInstance instance : mInstances) {
         instance.removeLastAttribute();
      }
   }
   
}
