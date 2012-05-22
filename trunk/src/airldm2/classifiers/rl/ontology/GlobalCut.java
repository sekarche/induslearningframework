package airldm2.classifiers.rl.ontology;

import java.util.List;
import java.util.Map;

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
   
}
