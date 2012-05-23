package airldm2.classifiers.rl.ontology;

import java.util.LinkedList;
import java.util.List;

import org.openrdf.model.URI;

import airldm2.util.CollectionUtil;

public class Cut {

   private final TBox mTBox;
   
   private List<URI> mCut;
   
   public Cut(TBox tBox, URI root) {
      this(tBox, new LinkedList<URI>());
      mCut.add(root);
   }
   
   public Cut(TBox tBox, List<URI> cut) {
      mTBox = tBox;
      mCut = cut;
   }
   
   public List<URI> get() {
      return mCut;
   }
   
   public int size() {
      return mCut.size();
   }
   
   public List<Cut> refine() {
      List<Cut> refinements = CollectionUtil.makeList();
      for (int i = 0; i < mCut.size(); i++) {
         URI cutI = mCut.get(i);
         List<URI> cutISub = mTBox.getDirectSubclass(cutI);
         if (cutISub.isEmpty()) continue;
         
         LinkedList<URI> copy = new LinkedList<URI>(mCut);
         copy.remove(i);
         copy.addAll(i, cutISub);
         refinements.add(new Cut(mTBox, copy));
      }
      
      return refinements;
   }

   @Override
   public String toString() {
      return mCut.toString();
   }
}
