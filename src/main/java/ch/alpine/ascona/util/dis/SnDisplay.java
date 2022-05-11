// code by jph
package ch.alpine.ascona.util.dis;

import java.io.Serializable;
import java.util.Random;

import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.sophus.api.TensorMetric;
import ch.alpine.sophus.decim.LineDistance;
import ch.alpine.sophus.hs.Biinvariant;
import ch.alpine.sophus.hs.MetricBiinvariant;
import ch.alpine.sophus.hs.sn.SnLineDistance;
import ch.alpine.sophus.hs.sn.SnManifold;
import ch.alpine.sophus.hs.sn.SnMetric;
import ch.alpine.sophus.hs.sn.SnRandomSample;
import ch.alpine.sophus.math.sample.RandomSampleInterface;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.lie.r2.CirclePoints;
import ch.alpine.tensor.sca.Abs;

/** symmetric positive definite 2 x 2 matrices */
public abstract class SnDisplay implements ManifoldDisplay, Serializable {
  private static final Tensor CIRCLE = CirclePoints.of(15).multiply(RealScalar.of(0.05)).unmodifiable();
  // ---
  private final int dimensions;

  protected SnDisplay(int dimensions) {
    this.dimensions = dimensions;
  }

  @Override // from ManifoldDisplay
  public final int dimensions() {
    return dimensions;
  }

  @Override // from ManifoldDisplay
  public final Tensor shape() {
    return CIRCLE;
  }

  @Override
  public final GeodesicSpace geodesicSpace() {
    return SnManifold.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public final TensorMetric biinvariantMetric() {
    return SnMetric.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public final Biinvariant biinvariant() {
    return MetricBiinvariant.EUCLIDEAN;
  }
  // @Override // from ManifoldDisplay
  // public final BiinvariantMean biinvariantMean() {
  // return SnFastMean.INSTANCE; // SnBiinvariantMean.of(Chop._05); // SnFastMean.INSTANCE;
  // }

  @Override // from ManifoldDisplay
  public final LineDistance lineDistance() {
    return SnLineDistance.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public final RandomSampleInterface randomSampleInterface() {
    RandomSampleInterface randomSampleInterface = SnRandomSample.of(dimensions);
    return new RandomSampleInterface() {
      @Override
      public Tensor randomSample(Random random) {
        Tensor xyz = randomSampleInterface.randomSample(random);
        xyz.set(Abs.FUNCTION, dimensions);
        return xyz;
      }
    };
  }

  @Override
  public final String toString() {
    return "S" + dimensions();
  }
}
