package airldm2.classifiers.rl;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openrdf.model.URI;

import airldm2.classifiers.Classifier;
import airldm2.classifiers.rl.estimator.AttributeEstimator;
import airldm2.classifiers.rl.estimator.ClassEstimator;
import airldm2.classifiers.rl.estimator.GaussianEstimator;
import airldm2.classifiers.rl.ontology.TBox;
import airldm2.core.LDInstance;
import airldm2.core.LDInstances;
import airldm2.core.rl.RDFDataDescriptor;
import airldm2.core.rl.RDFDataSource;
import airldm2.core.rl.RbcAttribute;
import airldm2.exceptions.RDFDatabaseException;
import airldm2.util.CollectionUtil;
import airldm2.util.Timer;


public class OntologyRRFClassifier extends Classifier {

   protected static Logger Log = Logger.getLogger("airldm2.classifiers.rl.OntologyRRFClassifier");
   static { Log.setLevel(Level.WARNING); }
      
   private LDInstances mInstances;
   private RDFDataDescriptor mDataDesc;
   private RDFDataSource mDataSource;

   private List<RbcAttribute> mNonTargetAttributes;
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
      mNonTargetAttributes = mDataDesc.getNonTargetAttributeList();
      
      mClassEst = new ClassEstimator();
      mClassEst.estimateParameters(mDataSource, mDataDesc);
      
      Log.warning("Retrieving TBox... ");
      
      mTBox = mDataSource.getTBox();
      
      for (int i = 0; i < mForestSize; i++) {
         Log.warning("Building tree " + i + " ... ");
         RDTClassifier tree = buildRDT();
         mForest.add(tree);
      }
      
      Timer.INSTANCE.stop("OntoRRF learning");
   }
   
   private RDTClassifier buildRDT() throws Exception {
      Set<RbcAttribute> nonTargetAttributes = CollectionUtil.makeSet();
      while (nonTargetAttributes.size() < mAttributeSampleSize) {
         nonTargetAttributes.add(mNonTargetAttributes.get(mRandom.nextInt(mNonTargetAttributes.size())));
      }
      
      List<RbcAttribute> nonTargetAttributeList = CollectionUtil.makeList();
      for (RbcAttribute att : nonTargetAttributes) {
         RbcAttribute extendedAtt = randomExtendHierarchy(att);
         
         nonTargetAttributeList.add(discretizeNonTargetAttribute(extendedAtt));
      }
      
      RDTClassifier rdt = new RDTClassifier(mDepthLimit);
      rdt.buildClassifier(mInstances, nonTargetAttributeList);
      return rdt;
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
      AttributeEstimator estimator = att.getEstimator();
      if (estimator instanceof GaussianEstimator) {
         estimator.setDataSource(mDataSource, mDataDesc, mClassEst);
         estimator.estimateParameters();
         
         Log.warning(estimator.toString());
         RbcAttribute discretizedAtt = ((GaussianEstimator) estimator).makeBinaryBinnedAttribute();
         return discretizedAtt;
      } else {
         return att;
      }
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
