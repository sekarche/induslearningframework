package airldm2.classifiers.rl.ontology;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import airldm2.core.rl.RbcAttribute;
import airldm2.util.CollectionUtil;

public class GlobalCut {

   private final TBox mTBox;
   
   private Map<RbcAttribute, Cut> mCuts;

   private GlobalCut(TBox tBox) {
      mTBox = tBox;
   }
   
   public GlobalCut(TBox tBox, List<RbcAttribute> atts) {
      this(tBox);
      mCuts = CollectionUtil.makeMap();
      for (RbcAttribute att : atts) {
         if (att.getHierarchyRoot() != null) {
            mCuts.put(att, new Cut(mTBox, att.getHierarchyRoot()));
         }
      }
   }

   public int size() {
      int size = 0;
      for (Cut c : mCuts.values()) {
         size += c.size();
      }
      return size;
   }
   
   public Cut getCut(RbcAttribute att) {
      return mCuts.get(att);
   }

   public void replace(RbcAttribute att, Cut cut) {
      mCuts.put(att, cut);
   }

   public GlobalCut copy() {
      GlobalCut copy = new GlobalCut(mTBox);
      copy.mCuts = CollectionUtil.makeMap(mCuts);
      return copy;
   }
   
   @Override
   public String toString() {
      StringBuilder b = new StringBuilder();
      b.append("[");
      for (Entry<RbcAttribute, Cut> entry : mCuts.entrySet()) {
         RbcAttribute key = entry.getKey();
         Cut value = entry.getValue();
         b.append(key.getName());
         b.append(": ");
         b.append(value);
         b.append(", ");
      }
      b.append("]");
      return b.toString();
   }

   public void resetLeafCuts() {
      for (RbcAttribute att : mCuts.keySet()) {
         if (att.getHierarchyRoot() != null) {
            Cut leafCut = mTBox.getLeafCut(att.getHierarchyRoot());
            mCuts.put(att, leafCut);
         }
      }
   }
   
}
