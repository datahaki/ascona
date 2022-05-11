// code by jph
package ch.alpine.ascona.util.dis;

import java.io.Serializable;

import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.sophus.api.TensorMetric;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.decim.LineDistance;
import ch.alpine.sophus.hs.Biinvariant;
import ch.alpine.sophus.hs.MetricBiinvariant;
import ch.alpine.sophus.lie.rn.RnBiinvariantMean;
import ch.alpine.sophus.lie.rn.RnGroup;
import ch.alpine.sophus.lie.rn.RnLineDistance;
import ch.alpine.sophus.lie.rn.RnMetric;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.PadRight;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.lie.r2.CirclePoints;

public abstract class RnDisplay implements ManifoldDisplay, Serializable {
  private static final Tensor CIRCLE = CirclePoints.of(15).multiply(RealScalar.of(0.04)).unmodifiable();
  private static final TensorUnaryOperator PAD = PadRight.zeros(2);
  // ---
  private final int dimensions;

  /* package */ RnDisplay(int dimensions) {
    this.dimensions = dimensions;
  }

  @Override // from ManifoldDisplay
  public final int dimensions() {
    return dimensions;
  }

  @Override // from ManifoldDisplay
  public Tensor shape() {
    return CIRCLE;
  }

  @Override // from ManifoldDisplay
  public final Tensor project(Tensor xya) {
    return xya.extract(0, dimensions);
  }

  @Override // from ManifoldDisplay
  public final TensorUnaryOperator tangentProjection(Tensor p) {
    return PAD;
  }

  @Override
  public GeodesicSpace geodesicSpace() {
    return RnGroup.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public final TensorMetric parametricDistance() {
    return RnMetric.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public final Biinvariant biinvariant() {
    return MetricBiinvariant.EUCLIDEAN;
  }

  @Override // from ManifoldDisplay
  public final BiinvariantMean biinvariantMean() {
    return RnBiinvariantMean.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public final LineDistance lineDistance() {
    return RnLineDistance.INSTANCE;
  }

  @Override // from Object
  public final String toString() {
    return "R" + dimensions;
  }
}
