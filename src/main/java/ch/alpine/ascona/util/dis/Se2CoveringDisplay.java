// code by jph
package ch.alpine.ascona.util.dis;

import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.sophus.lie.se2c.Se2CoveringGroup;
import ch.alpine.sophus.math.sample.RandomSampleInterface;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.num.Pi;
import ch.alpine.tensor.pdf.Distribution;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.UniformDistribution;

public class Se2CoveringDisplay extends Se2AbstractDisplay {
  public static final ManifoldDisplay INSTANCE = new Se2CoveringDisplay();

  // ---
  private Se2CoveringDisplay() {
    // ---
  }

  @Override // from ManifoldDisplay
  public Tensor xya2point(Tensor xya) {
    return xya.copy();
  }

  @Override
  public GeodesicSpace geodesicSpace() {
    return Se2CoveringGroup.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public RandomSampleInterface randomSampleInterface() {
    double lim = 3;
    Distribution distribution = UniformDistribution.of(-lim, lim);
    return random -> RandomVariate.of(distribution, random, 2).append( //
        RandomVariate.of(UniformDistribution.of(Pi.TWO.negate(), Pi.TWO), random));
  }

  @Override // from Object
  public String toString() {
    return "SE2C";
  }
}
