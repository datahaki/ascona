// code by jph
package ch.alpine.ascona.util.dis;

import java.io.Serializable;
import java.util.stream.Stream;

import ch.alpine.ascona.util.ren.EmptyRender;
import ch.alpine.ascona.util.ren.RenderInterface;
import ch.alpine.sophus.crv.TransitionSpace;
import ch.alpine.sophus.decim.LineDistance;
import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.sophus.lie.rn.RnGroup;
import ch.alpine.sophus.lie.rn.RnLineDistance;
import ch.alpine.sophus.lie.rn.RnTransitionSpace;
import ch.alpine.sophus.math.sample.BoxRandomSample;
import ch.alpine.sophus.math.sample.RandomSampleInterface;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.PadRight;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.lie.r2.CirclePoints;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;

public abstract class RnDisplay implements ManifoldDisplay, Serializable {
  private static final Clip CLIP = Clips.absolute(1);
  private static final Tensor CIRCLE = CirclePoints.of(15).multiply(RealScalar.of(0.06)).unmodifiable();
  private static final TensorUnaryOperator LIFT = PadRight.zeros(3);
  // ---
  private final int dimensions;

  protected RnDisplay(int dimensions) {
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
  public final Tensor xya2point(Tensor xya) {
    return xya.extract(0, dimensions);
  }

  @Override // from ManifoldDisplay
  public final TensorUnaryOperator tangentProjection(Tensor p) {
    // TODO ASCONA not clear
    return PadRight.zeros(2);
  }

  @Override // from ManifoldDisplay
  public final GeodesicSpace geodesicSpace() {
    return RnGroup.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public final TransitionSpace transitionSpace() {
    return RnTransitionSpace.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public final LineDistance lineDistance() {
    return RnLineDistance.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public final RenderInterface background() {
    return EmptyRender.INSTANCE;
    // return AxesRender.INSTANCE;
  }

  public final CoordinateBoundingBox coordinateBoundingBox() {
    return CoordinateBoundingBox.of(Stream.generate(() -> CLIP).limit(dimensions));
  }

  @Override // from ManifoldDisplay
  public final RandomSampleInterface randomSampleInterface() {
    return BoxRandomSample.of(coordinateBoundingBox());
  }

  @Override // from ManifoldDisplay
  public final Tensor point2xya(Tensor p) {
    return LIFT.apply(p);
  }
}
