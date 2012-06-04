package airldm2.classifiers.rl.ontology;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
   
   public Cut refineAll() {
      List<URI> refinement = CollectionUtil.makeList();
      for (int i = 0; i < mCut.size(); i++) {
         URI cutI = mCut.get(i);
         List<URI> cutISub = mTBox.getDirectSubclass(cutI);
         if (cutISub.isEmpty()) {
            refinement.add(cutI);
         } else {
            refinement.addAll(cutISub);
         }
      }
      return new Cut(mTBox, refinement);
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
   
   public Cut abstractCut() {
      if (size() <= 1) return null;
      
      Set<URI> oldCut = CollectionUtil.makeSet(mCut);
      Set<URI> newCut = CollectionUtil.makeSet();
      for (URI uri : mCut) {
         List<URI> siblings = mTBox.getSiblings(uri);
                 
         if (oldCut.containsAll(siblings)) {
            oldCut.removeAll(siblings);
            URI sup = mTBox.getDirectSuperclass(uri);
            newCut.add(sup);
         }
      }
      
      newCut.addAll(oldCut);      
      return new Cut(mTBox, CollectionUtil.makeList(newCut));
   }

   @Override
   public String toString() {
      return mCut.toString();
   }
}
