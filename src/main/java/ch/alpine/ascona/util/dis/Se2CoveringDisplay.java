// code by jph
package ch.alpine.ascona.util.dis;

import java.util.Random;

import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.sophus.api.TensorMetric;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.hs.r2.Se2CoveringParametric;
import ch.alpine.sophus.lie.LieGroup;
import ch.alpine.sophus.lie.se2c.Se2CoveringBiinvariantMean;
import ch.alpine.sophus.lie.se2c.Se2CoveringGeodesic;
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
  public GeodesicSpace geodesicSpace() {
    return Se2CoveringGeodesic.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public Tensor project(Tensor xya) {
    return xya.copy();
  }

  @Override // from ManifoldDisplay
  public LieGroup lieGroup() {
    return Se2CoveringGroup.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public HomogeneousSpace hsManifold() {
    return Se2CoveringGroup.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public TensorMetric parametricDistance() {
    return Se2CoveringParametric.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public BiinvariantMean biinvariantMean() {
    return Se2CoveringBiinvariantMean.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public RandomSampleInterface randomSampleInterface() {
    double lim = 3;
    Distribution distribution = UniformDistribution.of(-lim, lim);
    return new RandomSampleInterface() {
      @Override
      public Tensor randomSample(Random random) {
        return RandomVariate.of(distribution, random, 2).append( //
            RandomVariate.of(UniformDistribution.of(Pi.TWO.negate(), Pi.TWO), random));
      }
    };
  }

  @Override // from Object
  public String toString() {
    return "SE2C";
  }
}
