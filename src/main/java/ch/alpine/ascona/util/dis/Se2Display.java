// code by jph
package ch.alpine.ascona.util.dis;

import java.util.Random;

import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.sophus.lie.se2.Se2Group;
import ch.alpine.sophus.lie.so2.So2;
import ch.alpine.sophus.math.sample.RandomSampleInterface;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.num.Pi;
import ch.alpine.tensor.pdf.Distribution;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.UniformDistribution;

public class Se2Display extends Se2AbstractDisplay {
  public static final ManifoldDisplay INSTANCE = new Se2Display();

  // ---
  private Se2Display() {
    // ---
  }

  @Override // from ManifoldDisplay
  public Tensor project(Tensor xya) {
    Tensor xym = xya.copy();
    xym.set(So2.MOD, 2);
    return xym;
  }

  @Override
  public GeodesicSpace geodesicSpace() {
    return Se2Group.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public RandomSampleInterface randomSampleInterface() {
    double lim = 3;
    Distribution distribution = UniformDistribution.of(-lim, lim);
    return new RandomSampleInterface() {
      @Override
      public Tensor randomSample(Random random) {
        return RandomVariate.of(distribution, random, 2).append( //
            RandomVariate.of(UniformDistribution.of(Pi.VALUE.negate(), Pi.VALUE), random));
      }
    };
  }

  @Override // from Object
  public String toString() {
    return "SE2";
  }
}
