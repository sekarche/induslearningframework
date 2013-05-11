package airldm2.classifiers.rl.ontology;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;

import airldm2.util.CollectionUtil;
import airldm2.util.MathUtil;


public class TBoxHelper {

   static class Stat {
      int[] Count;
      double TermXY;
      double TermY;
      
      public Stat(int classSize) {
         Count = new int[classSize];
      }
      
      public void addCount(Stat o) {
         MathUtil.addTo(Count, o.Count);
      }

      public void update() {
         int sum = 0;
         TermXY = 0;
         for (int i = 0; i < Count.length; i++) {
            sum += Count[i];
            TermXY += Count[i] * MathUtil.lg(Count[i]);
         }
         TermY = sum * MathUtil.lg(sum);
      }
   }
   
   public class CutStat {
      Cut Cut;
      
      double XYSum;
      double YSum;
      int[] XSums;
      double MI;
      
      public void updateMutualInfo() {
         MI = XYSum - YSum;
         double currentTermX = 0.0;
         for (int i = 0; i < XSums.length; i++) {
            currentTermX += XSums[i] * MathUtil.lg(XSums[i]);
         }
         MI = MI - currentTermX + mSum * MathUtil.lg(mSum);
         MI /= mSum;
//       System.out.println(XYSum + " " + YSum + " " + Arrays.toString(XSums) + " = " + MI);
      }

      public CutStat copy(Cut cut) {
         CutStat copy = new CutStat();
         copy.Cut = cut;
         copy.XYSum = XYSum;
         copy.YSum = YSum;
         copy.XSums = new int[XSums.length];
         System.arraycopy(XSums, 0, copy.XSums, 0, XSums.length);
         copy.MI = MI;
         return copy;
      }

      public void remove(Stat stat) {
         XYSum -= stat.TermXY;
         YSum -= stat.TermY;
         for (int i = 0; i < XSums.length; i++) {
            XSums[i] -= stat.Count[i];
         }
      }

      public void add(Stat stat) {
         XYSum += stat.TermXY;
         YSum += stat.TermY;
         for (int i = 0; i < XSums.length; i++) {
            XSums[i] += stat.Count[i];
         }
      }

   }
   
   private URI mRoot;
   private int mClassSize;
   
   private TBox mTBox;
   private Map<URI,Stat> mStat;
   
   private CutStat mCurrentCutStat;
   private int mSum;
   
   
   public TBoxHelper(URI root) {
      this(new TBox(), root);
   }
   
   public TBoxHelper(TBox tBox, URI root) {
      mTBox = tBox;
      mRoot = root;
      
      mStat = CollectionUtil.makeMap();
   }

   public void setClassSize(int size) {
      mClassSize = size;
   }

   public TBox getTBox() {
      return mTBox;
   }
   
   public void initCount() {
      for (URI leaf : mTBox.getLeaves()) {
         Stat s = getStat(leaf);
         //Laplace Correction
         Arrays.fill(s.Count, 1);
      }
   }
   
   public void addCount(URI synset, int c, int i) {
      Stat cs = getStat(synset);
      cs.Count[c] += i;
   }
   
   private Stat getStat(URI n) {
      Stat nStat = mStat.get(n);
      if (nStat == null) {
         nStat = new Stat(mClassSize);
         mStat.put(n, nStat);
      }
      
      return nStat;
   }

   public void inferAllCounts() {
      List<List<URI>> layers = mTBox.getLayers(mRoot);
      for (int i = layers.size() - 1; i >= 0; i--) {
         List<URI> layer = layers.get(i);
         for (URI n : layer) {
            Stat nStat = getStat(n);
            
            if (!mTBox.isLeaf(n)) {
               for (URI sub : mTBox.getDirectSubclass(n)) {
                  Stat subStat = getStat(sub);
                  nStat.addCount(subStat);
               }
            }
            
            nStat.update();
         }
      }
   }
   
   public void initSearch() {
      mCurrentCutStat = new CutStat();
      mCurrentCutStat.Cut = mTBox.getRootCut(mRoot);
      Stat rootStat = getStat(mRoot);
      mCurrentCutStat.XYSum = rootStat.TermXY;
      mCurrentCutStat.YSum = rootStat.TermY;
      mCurrentCutStat.XSums = new int[mClassSize];
      System.arraycopy(rootStat.Count, 0, mCurrentCutStat.XSums, 0, mClassSize);
      mSum = MathUtil.sum(rootStat.Count);
      mCurrentCutStat.updateMutualInfo();
   }

   private CutStat searchNextCut() {
      CutStat bestNewCut = null;
      
      List<URI> cutList = mCurrentCutStat.Cut.get();
      for (int i = 0; i < cutList.size(); i++) {
         URI cutI = cutList.get(i);
         List<URI> cutISub = mTBox.getDirectSubclass(cutI);
         while (cutISub.size() == 1) {
            cutISub = mTBox.getDirectSubclass(cutISub.get(0));
         }
         if (cutISub.isEmpty()) continue;

         if (bestNewCut == null) {
            LinkedList<URI> copy = new LinkedList<URI>(cutList);
            copy.remove(i);
            copy.addAll(i, cutISub);
            bestNewCut = mCurrentCutStat.copy(new Cut(mTBox, copy));
            augmentStat(bestNewCut, cutI, cutISub, true);
         } else {
            augmentStat(mCurrentCutStat, cutI, cutISub, true);
            if (mCurrentCutStat.MI > bestNewCut.MI) {
               LinkedList<URI> copy = new LinkedList<URI>(cutList);
               copy.remove(i);
               copy.addAll(i, cutISub);
               bestNewCut = mCurrentCutStat.copy(new Cut(mTBox, copy));
            }
            augmentStat(mCurrentCutStat, cutI, cutISub, false);
         }
      }
      
      return bestNewCut;
   }
   
   public int searchCut(int minSize) {
      if (minSize < 0) {
         CutStat bestNewCut = searchNextCut();
         if (bestNewCut == null) return -1;
         
         mCurrentCutStat = bestNewCut;
         return mCurrentCutStat.Cut.size();
      }
      
      while (mCurrentCutStat.Cut.size() < minSize) {
         CutStat bestNewCut = searchNextCut();
         if (bestNewCut == null) return -1;
         
         mCurrentCutStat = bestNewCut;
      }
      return mCurrentCutStat.Cut.size();
   }

   private void augmentStat(CutStat stat, URI oldNode, List<URI> newNodes, boolean isForward) {
      if (isForward) {
         Stat oldStat = mStat.get(oldNode);
         stat.remove(oldStat);
         for (URI i : newNodes) {
            Stat newStat = mStat.get(i);
            stat.add(newStat);
         }
      } else {
         Stat oldStat = mStat.get(oldNode);
         stat.add(oldStat);
         for (URI i : newNodes) {
            Stat newStat = mStat.get(i);
            stat.remove(newStat);
         }
      }
      
      stat.updateMutualInfo();
   }

   
   private Map<URI,URI> mCutMap;
   public void updateCutMap() {
      mCutMap = CollectionUtil.makeMap();
      
      for (URI c : mCurrentCutStat.Cut.get()) {
         mCutMap.put(c, c);
         for (URI sub : mTBox.getAllSubclasses(c)) {
            mCutMap.put(sub, c);
         }
      }
      
      System.out.println(mCurrentCutStat.Cut.size() + ", " + mCurrentCutStat.MI);
   }
   
   public URI getProjectedCut(URI i) {
      return mCutMap.get(i);
   }

   public Cut getCurrentCut() {
      return mCurrentCutStat.Cut;
   }
   
   public CutProfile getMICutProfile() {
      initSearch();
      CutProfile profile = new CutProfile();
      
      int size = 0;
      while (size >= 0) {
         profile.add(mCurrentCutStat.Cut, mCurrentCutStat.MI);
         size = searchCut(-1);
      }
      
      return profile;
   }
   
   
   public static TBoxHelper create(TBox tBox, URI hierarchyRoot, List<Map<URI, Double>> valueHistograms) {
      TBoxHelper helper = new TBoxHelper(tBox, hierarchyRoot);
      helper.setClassSize(valueHistograms.size());
      
      helper.initCount();
      for (URI leaf : helper.mTBox.getLeaves()) {
         for (int c = 0; c < valueHistograms.size(); c++) {
            Map<URI, Double> map = valueHistograms.get(c);
            Double count = map.get(leaf);
            if (count != null) {
               helper.addCount(leaf, c, (int) Math.round(count));
            }
         }
      }
      
      helper.inferAllCounts();
      return helper;
   }

   
}
