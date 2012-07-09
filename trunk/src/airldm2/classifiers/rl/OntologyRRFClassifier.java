package airldm2.classifiers.rl;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openrdf.model.URI;

import explore.RDFDataDescriptorEnhancer;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import airldm2.classifiers.Classifier;
import airldm2.classifiers.rl.estimator.ClassEstimator;
import airldm2.classifiers.rl.ontology.TBox;
import airldm2.core.LDInstance;
import airldm2.core.LDInstances;
import airldm2.core.rl.OntologyEnumType;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.core.rl.RbcAttributeValue;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.util.CollectionUtil;
import airldm2.util.Timer;


public class OntologyRRFClassifier extends Classifier {

   protected static Logger Log = Logger.getLogger("airldm2.classifiers.rl.OntologyRRFClassifier");
   static { Log.setLevel(Level.WARNING); }
      
   private LDInstances mInstances;
   private RDFDataDescriptor mDataDesc;
   private RDFDataSource mDataSource;

   private ClassEstimator mClassEst;
   
   private List<RDTClassifier> mForest;

   private final int mForestSize;
   private final int mAttributeSampleSize;
   private final int mDepthLimit;
   private Random mRandom;
   
   private TBox mTBox;
   
   public OntologyRRFClassifier(int forestSize, int attributeSampleSize, int depthLimit) {
      mForestSize = forestSize;
      mAttributeSampleSize = attributeSampleSize;
      mDepthLimit = depthLimit;
      mRandom = new Random(0);
      mForest = CollectionUtil.makeList();
   }
   
   @Override
   public void buildClassifier(LDInstances instances) throws Exception {
      Timer.INSTANCE.start("OntoRRF learning");
      
      mInstances = instances;
      mDataDesc = (RDFDataDescriptor) instances.getDesc();
      mDataSource = (RDFDataSource) instances.getDataSource();
      
      new RDFDataDescriptorEnhancer(mDataSource).fillDomain(mDataDesc);
      
      mClassEst = new ClassEstimator();
      mClassEst.estimateParameters(mDataSource, mDataDesc);
      
      Log.warning("Retrieving TBox... ");
      
      mTBox = mDataSource.getTBox();
      
      for (int i = 0; i < mForestSize; i++) {
         Log.warning("Building tree " + i + " ... ");
         RDTClassifier tree = buildRDT();
         mForest.add(tree);
      }
      
      Log.warning("Final forest: " + mForest);
      
      Timer.INSTANCE.stop("OntoRRF learning");
   }
   
   private RDTClassifier buildRDT() throws Exception {
      List<RbcAttribute> atts = CollectionUtil.makeList();
      for (RbcAttribute att : mDataDesc.getNonTargetAttributeList()) {
         atts.add(discretizeNonTargetAttribute(att));
      }
      
      List<RbcAttributeValue> attValues = makeRandomAttValues(atts);
      
      RDTClassifier rdt = new RDTClassifier(mDepthLimit);
      rdt.buildClassifier(mInstances, attValues);
      return rdt;
   }
   
   private List<RbcAttributeValue> makeRandomAttValues(List<RbcAttribute> atts) {
      Set<RbcAttributeValue> attValues = CollectionUtil.makeSet();
      while (attValues.size() < mAttributeSampleSize) {
         RbcAttribute att = atts.get(mRandom.nextInt(atts.size()));
         if (att.isHierarchicalHistogram() || att.isHierarchicalSet()) {
            List<List<URI>> layers = mTBox.getLayers(att.getHierarchyRoot());
            
            int level = mRandom.nextInt(layers.size() - 1) + 1;
            List<URI> cut = layers.get(level);
            int cutIndex = mRandom.nextInt(cut.size());
            String key = cut.get(cutIndex).stringValue();
            
            Log.info("Level size=" + layers.size() + " Level=" + level + " Index=" + cutIndex);
            
            RbcAttribute attCopy = att.copy();
            OntologyEnumType cutEnum = new OntologyEnumType(mTBox, cut);
            attCopy.setValueType(cutEnum);
            
            attValues.add(new RbcAttributeValue(attCopy, key));
         } else {
            throw new NotImplementedException();
         }
      }
      
      return CollectionUtil.makeList(attValues);
   }

   private RbcAttribute randomExtendHierarchy(RbcAttribute att) {
      URI hierarchyRoot = att.getHierarchyRoot();
      if (hierarchyRoot == null) return att;
      
      List<URI> allHierarchy = CollectionUtil.makeList();
      allHierarchy.add(hierarchyRoot);
      allHierarchy.addAll(mTBox.getAllSubclasses(hierarchyRoot));
      URI randomURI = allHierarchy.get(mRandom.nextInt(allHierarchy.size()));
      
      return att.extendWithHierarchy(randomURI, mTBox.isLeaf(randomURI));
   }

   private RbcAttribute discretizeNonTargetAttribute(RbcAttribute att) throws RDFDatabaseException {
//      AttributeEstimator estimator = att.getEstimator();
//      if (estimator instanceof GaussianEstimator) {
//         estimator.setDataSource(mDataSource, mDataDesc, mClassEst);
//         estimator.estimateParameters();
//         
//         Log.warning(estimator.toString());
//         RbcAttribute discretizedAtt = ((GaussianEstimator) estimator).makeBinaryBinnedAttribute();
//         return discretizedAtt;
//      } else {
         return att;
//      }
   }
   
   public List<RDTClassifier> getForest() {
      return mForest;
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

}
