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
   
   public Cut copy() {
      return new Cut(mTBox, new LinkedList<URI>(mCut));
   }
   
   public List<URI> get() {
      return mCut;
   }
   
   public TBox getTBox() {
      return mTBox;
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
         while (cutISub.size() == 1) {
            cutISub = mTBox.getDirectSubclass(cutISub.get(0));
         }
         if (cutISub.isEmpty()) continue;
         
         LinkedList<URI> copy = new LinkedList<URI>(mCut);
         copy.remove(i);
         copy.addAll(i, cutISub);
         refinements.add(new Cut(mTBox, copy));
      }
      
      return refinements;
   }
   
   public boolean refineGreedyBFS() {
      for (int i = 0; i < mCut.size(); i++) {
         URI cutI = mCut.get(i);
         List<URI> cutISub = mTBox.getDirectSubclass(cutI);
         if (cutISub.isEmpty()) continue;
         
         mCut.remove(i);
         mCut.addAll(cutISub);
         return true;
      }
      return false;
   }
   
   private Set<URI> mCutSet;
   public void optimizeAbstraction() {
      mCutSet = CollectionUtil.makeSet(mCut);
   }
   
   public URI abstractCut() {
      if (size() <= 1) return null;
      
      for (int i = 0; i < mCut.size(); i++) {
         URI uri = mCut.get(i);
         List<URI> siblings = mTBox.getSiblings(uri);
         
         if (mCutSet.containsAll(siblings)) {
            mCut.removeAll(siblings);
            mCutSet.removeAll(siblings);
            
            URI sup = mTBox.getDirectSuperclass(uri);
            mCut.add(sup);
            mCutSet.add(sup);
            return sup;
         }
      }
      
      throw new RuntimeException("abstractCut");
   }

   @Override
   public String toString() {
      return mCut.toString();
   }

}
