package airldm2.classifiers.rl;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openrdf.model.URI;

import airldm2.classifiers.Classifier;
import airldm2.classifiers.rl.estimator.AttributeEstimator;
import airldm2.classifiers.rl.estimator.ClassEstimator;
import airldm2.classifiers.rl.estimator.GaussianEstimator;
import airldm2.classifiers.rl.ontology.Cut;
import airldm2.classifiers.rl.ontology.GlobalCut;
import airldm2.classifiers.rl.ontology.TBox;
import airldm2.core.LDInstance;
import airldm2.core.LDInstances;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.util.CollectionUtil;


public class OntologyRDTClassifier extends Classifier {

   protected static Logger Log = Logger.getLogger("airldm2.classifiers.rl.OntologyRDTClassifier");
   static { Log.setLevel(Level.INFO); }
      
   private LDInstances mInstances;
   private RDFDataDescriptor mDataDesc;
   private RDFDataSource mDataSource;

   private List<RbcAttribute> mNonTargetAttributes;
   private ClassEstimator mClassEst;
   
   private TBox mTBox;
   private GlobalCut mGlobalCut;
   private RDTClassifier mBestRDT;
   
   
   @Override
   public void buildClassifier(LDInstances instances) throws Exception {
      mInstances = instances;
      mDataDesc = (RDFDataDescriptor) instances.getDesc();
      mDataSource = (RDFDataSource) instances.getDataSource();
      mNonTargetAttributes = mDataDesc.getNonTargetAttributeList();
      
      mClassEst = new ClassEstimator();
      mClassEst.estimateParameters(mDataSource, mDataDesc);
      
      Log.info("Retrieving TBox... ");
      
      mTBox = mDataSource.getTBox();
      mGlobalCut = new GlobalCut(mTBox, mNonTargetAttributes);
      
      Log.info("Build tree for root cut... ");
      mBestRDT = buildRDT(mGlobalCut);
      
      Log.info("Searching... ");
      
      //Greedy search global cut
      double bestScore = mBestRDT.getTreeScore();
      while (true) {
         double newBestScore = Double.NEGATIVE_INFINITY;
         GlobalCut newBestGlobalCut = null;
         RDTClassifier newbestRDT = null;
         
         for (RbcAttribute att : mNonTargetAttributes) {
            Cut attCut = mGlobalCut.getCut(att);
            if (attCut == null) continue;
            
            Cut attRefinement = attCut.refineAll();
            if (attRefinement == null) continue;
            
            GlobalCut globalCut = mGlobalCut.copy();
            globalCut.replace(att, attRefinement);
            
            Log.info("Building with new cut: " + globalCut);
            
            RDTClassifier rdtNew = buildRDT(globalCut);
            
            double score = rdtNew.getTreeScore();
            
            if (score > newBestScore) {
               Log.info("New score " + score + " " + globalCut.toString());
               newBestScore = score;
               newBestGlobalCut = globalCut;
               newbestRDT = rdtNew;
            }
         }         
         
         if (newBestScore > bestScore) {
            bestScore = newBestScore;
            mGlobalCut = newBestGlobalCut;
            mBestRDT = newbestRDT;
            Log.info("New best global cut found with score " + newBestScore);
         } else {
            break;
         }
      }
      
      Log.info("Final tree: " + mBestRDT.getTree());
   }
   
   private RDTClassifier buildRDT(GlobalCut globalCut) throws Exception {
      List<RbcAttribute> nonTargetAttributes = CollectionUtil.makeList();
      
      for (RbcAttribute att : mNonTargetAttributes) {
         Cut attCut = globalCut.getCut(att);
         if (attCut == null) {
            RbcAttribute discretized = discretizeNonTargetAttribute(att);
            nonTargetAttributes.add(discretized);
         } else {
            for (URI uri : attCut.get()) {
               RbcAttribute extendedAtt = att.extendWithHierarchy(uri, mTBox.isLeaf(uri));
               RbcAttribute discretized = discretizeNonTargetAttribute(extendedAtt);
               nonTargetAttributes.add(discretized);
            }
         }
      }
      
      RDTClassifier rdt = new RDTClassifier();
      rdt.buildClassifier(mInstances, nonTargetAttributes);
      return rdt;
   }
   
   private RbcAttribute discretizeNonTargetAttribute(RbcAttribute att) throws RDFDatabaseException {
      AttributeEstimator estimator = att.getEstimator();
      if (estimator instanceof GaussianEstimator) {
         estimator.setDataSource(mDataSource, mDataDesc, mClassEst);
         estimator.estimateParameters();
         
         Log.info(estimator.toString());
         RbcAttribute discretizedAtt = ((GaussianEstimator) estimator).makeBinaryBinnedAttribute();
         return discretizedAtt;
      } else {
         return att;
      }
   }
   
   public List<RbcAttribute> getTreeAttributes() {
      return mBestRDT.getTreeAttributes();
   }
   
   @Override
   public double classifyInstance(LDInstance instance) throws Exception {
      return 0;
   }

   @Override
   public double[] distributionForInstance(LDInstance instance)
         throws Exception {
      return null;
   }

   public double classifyInstance(AggregatedInstance instance) {
      return mBestRDT.classifyInstance(instance);
   }
   
   public double[] distributionForInstance(AggregatedInstance instance) {
      return mBestRDT.distributionForInstance(instance);
   }

}
