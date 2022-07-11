// code by jph
package ch.alpine.ascona.util.dis;

import java.io.Serializable;

import ch.alpine.ascona.util.ren.EmptyRender;
import ch.alpine.ascona.util.ren.RenderInterface;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.sophus.decim.LineDistance;
import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.sophus.lie.so3.Rodrigues;
import ch.alpine.sophus.lie.so3.So3Group;
import ch.alpine.sophus.lie.so3.So3RandomSample;
import ch.alpine.sophus.math.sample.RandomSampleInterface;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.lie.r2.CirclePoints;
import ch.alpine.tensor.nrm.Vector2Norm;

/** orthogonal 3 x 3 matrices */
public class So3Display implements ManifoldDisplay, Serializable {
  private static final Tensor TRIANGLE = CirclePoints.of(3).multiply(RealScalar.of(0.4)).unmodifiable();
  // TODO ASCONA radius == 1
  private static final Scalar RADIUS = RealScalar.of(7);
  // ---
  public static final ManifoldDisplay INSTANCE = new So3Display(RADIUS);
  // ---
  private final Scalar radius;

  public So3Display(Scalar radius) {
    this.radius = radius;
  }

  @Override // from ManifoldDisplay
  public int dimensions() {
    return 3;
  }

  @Override // from ManifoldDisplay
  public Tensor shape() {
    return TRIANGLE;
  }

  @Override // from ManifoldDisplay
  public Tensor xya2point(Tensor xya) {
    Tensor axis = xya.divide(radius);
    Scalar norm = Vector2Norm.of(axis);
    if (Scalars.lessThan(RealScalar.ONE, norm))
      axis = axis.divide(norm);
    return Rodrigues.vectorExp(axis);
  }

  @Override // from ManifoldDisplay
  public Tensor point2xya(Tensor p) {
    return Rodrigues.INSTANCE.vectorLog(p).multiply(radius);
  }

  @Override // from ManifoldDisplay
  public final TensorUnaryOperator tangentProjection(Tensor xyz) {
    return null;
  }

  @Override // from ManifoldDisplay
  public Tensor matrixLift(Tensor xyz) {
    return GfxMatrix.translation(point2xy(xyz));
  }

  @Override
  public GeodesicSpace geodesicSpace() {
    return So3Group.INSTANCE;
  }

  @Override
  public final LineDistance lineDistance() {
    return null; // TODO ASCONA ALG line distance should be similar to s^3
  }

  @Override
  public RandomSampleInterface randomSampleInterface() {
    return So3RandomSample.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public RenderInterface background() {
    return EmptyRender.INSTANCE;
  }
}
