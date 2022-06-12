// code by jph
package ch.alpine.ascona.util.dis;

import ch.alpine.ascona.util.arp.HsArrayPlot;
import ch.alpine.ascona.util.win.RenderInterface;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.UnitVector;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.nrm.Vector2Norm;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;

/** symmetric positive definite 2 x 2 matrices */
public class S1Display extends SnDisplay {
  public static final ManifoldDisplay INSTANCE = new S1Display();

  // ---
  private S1Display() {
    super(1);
  }

  @Override // from ManifoldDisplay
  public Tensor project(Tensor xya) {
    Tensor xy = xya.extract(0, 2);
    Scalar norm = Vector2Norm.of(xy);
    return Scalars.isZero(norm) //
        ? UnitVector.of(2, 0)
        : xy.divide(norm);
  }

  @Override // from ManifoldDisplay
  public TensorUnaryOperator tangentProjection(Tensor p) {
    return null;
  }

  @Override // from ManifoldDisplay
  public Tensor toPoint(Tensor xy) {
    return xy.copy();
  }

  @Override // from ManifoldDisplay
  public Tensor matrixLift(Tensor xy) {
    return GfxMatrix.translation(toPoint(xy));
  }

  @Override
  public Tensor lift(Tensor p) {
    return p.copy().append(RealScalar.ZERO);
  }

  @Override
  public RenderInterface background() {
    return S1Background.INSTANCE;
  }
}
