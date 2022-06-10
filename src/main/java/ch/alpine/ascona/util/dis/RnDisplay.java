// code by jph
package ch.alpine.ascona.util.dis;

import java.io.Serializable;
import java.util.stream.Stream;

import ch.alpine.ascona.util.ren.EmptyRender;
import ch.alpine.ascona.util.win.RenderInterface;
import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.sophus.api.TensorMetric;
import ch.alpine.sophus.decim.LineDistance;
import ch.alpine.sophus.hs.Biinvariant;
import ch.alpine.sophus.hs.MetricBiinvariant;
import ch.alpine.sophus.lie.rn.RnGroup;
import ch.alpine.sophus.lie.rn.RnLineDistance;
import ch.alpine.sophus.lie.rn.RnMetric;
import ch.alpine.sophus.math.sample.BoxRandomSample;
import ch.alpine.sophus.math.sample.RandomSampleInterface;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.PadRight;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.lie.r2.CirclePoints;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Clips;

public abstract class RnDisplay implements ManifoldDisplay, Serializable {
  private static final Scalar RADIUS = RealScalar.of(1.0);
  private static final Tensor CIRCLE = CirclePoints.of(15).multiply(RealScalar.of(0.06)).unmodifiable();
  private static final TensorUnaryOperator LIFT = PadRight.zeros(3);
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
  public final Tensor shape() {
    return CIRCLE;
  }

  @Override // from ManifoldDisplay
  public final Tensor project(Tensor xya) {
    return xya.extract(0, dimensions);
  }

  @Override // from ManifoldDisplay
  public final TensorUnaryOperator tangentProjection(Tensor p) {
    // TODO ASCONA not clear
    return PadRight.zeros(2);
  }

  @Override
  public final GeodesicSpace geodesicSpace() {
    return RnGroup.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public final TensorMetric biinvariantMetric() {
    return RnMetric.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public final Biinvariant biinvariant() {
    return MetricBiinvariant.EUCLIDEAN;
  }

  @Override // from ManifoldDisplay
  public final LineDistance lineDistance() {
    return RnLineDistance.INSTANCE;
  }

  @Override
  public final RenderInterface background() {
    return EmptyRender.INSTANCE;
    // return AxesRender.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public final CoordinateBoundingBox coordinateBoundingBox() {
    return CoordinateBoundingBox.of(Stream.generate(() -> Clips.absolute(RADIUS)).limit(dimensions));
  }

  @Override // from ManifoldDisplay
  public final RandomSampleInterface randomSampleInterface() {
    return BoxRandomSample.of(coordinateBoundingBox());
  }

  @Override
  public final Tensor lift(Tensor p) {
    return LIFT.apply(p);
  }

  @Override // from Object
  public final String toString() {
    return "R" + dimensions;
  }
}
