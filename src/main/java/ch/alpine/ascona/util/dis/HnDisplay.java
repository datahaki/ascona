// code by jph
package ch.alpine.ascona.util.dis;

import java.io.Serializable;
import java.util.Random;

import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.sophus.api.TensorMetric;
import ch.alpine.sophus.crv.d2.StarPoints;
import ch.alpine.sophus.decim.LineDistance;
import ch.alpine.sophus.hs.Biinvariant;
import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.sophus.hs.hn.HnManifold;
import ch.alpine.sophus.hs.hn.HnMetric;
import ch.alpine.sophus.hs.hn.HnMetricBiinvariant;
import ch.alpine.sophus.hs.hn.HnWeierstrassCoordinate;
import ch.alpine.sophus.math.sample.RandomSampleInterface;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.PadRight;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.pdf.Distribution;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.UniformDistribution;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;

/** symmetric positive definite 2 x 2 matrices */
public abstract class HnDisplay implements ManifoldDisplay, Serializable {
  private static final Tensor STAR_POINTS = StarPoints.of(6, 0.12, 0.04).unmodifiable();
  protected static final Clip CLIP = Clips.absolute(2.5);
  private static final TensorUnaryOperator LIFT = PadRight.zeros(3);
  // ---
  private final int dimensions;

  protected HnDisplay(int dimensions) {
    this.dimensions = dimensions;
  }

  @Override
  public final int dimensions() {
    return dimensions;
  }

  @Override // from ManifoldDisplay
  public final Tensor project(Tensor xya) {
    return HnWeierstrassCoordinate.toPoint(xya.extract(0, dimensions));
  }

  @Override // from ManifoldDisplay
  public final TensorUnaryOperator tangentProjection(Tensor xyz) {
    return null;
  }

  @Override // from ManifoldDisplay
  public final Tensor matrixLift(Tensor p) {
    return GfxMatrix.translation(p);
  }

  @Override // from ManifoldDisplay
  public final Tensor shape() {
    return STAR_POINTS;
  }

  @Override
  public final GeodesicSpace geodesicSpace() {
    return HnManifold.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public final TensorMetric biinvariantMetric() {
    return HnMetric.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public final Biinvariant biinvariant() {
    return HnMetricBiinvariant.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public final LineDistance lineDistance() {
    return null;
  }

  @Override // from ManifoldDisplay
  public final RandomSampleInterface randomSampleInterface() {
    Distribution distribution = UniformDistribution.of(CLIP);
    return new RandomSampleInterface() {
      @Override
      public Tensor randomSample(Random random) {
        // return VectorQ.requireLength(RandomVariate.of(distribution, random, 2).append(RealScalar.ZERO), 3);
        return HnWeierstrassCoordinate.toPoint(RandomVariate.of(distribution, random, dimensions));
      }
    };
  }

  @Override
  public final Tensor lift(Tensor p) {
    return LIFT.apply(p.extract(0, dimensions));
  }

  @Override
  public final String toString() {
    return "H" + dimensions();
  }
}
