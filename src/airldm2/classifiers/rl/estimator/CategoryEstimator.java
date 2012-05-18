package airldm2.classifiers.rl.estimator;

import airldm2.core.rl.RbcAttribute;

public class CategoryEstimator extends MultinomialEstimator {

   public CategoryEstimator(RbcAttribute att) {
      super(att);
   }

   @Override
   public double computeLikelihood(int classIndex, AttributeValue v) {
      if (v instanceof Null) return 1.0;
      if (!(v instanceof Category)) 
         throw new IllegalArgumentException("Error: value " + v + " is not a Category for CategoryEstimator.");
      
      Category val = (Category) v;
      Histogram oneOfK = Histogram.make1ofK(val.getIndex(), mAttribute.getDomainSize());
      return super.computeLikelihood(classIndex, oneOfK);
   }

}
