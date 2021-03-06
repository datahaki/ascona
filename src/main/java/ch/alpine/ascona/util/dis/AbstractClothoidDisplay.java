// code by jph
package ch.alpine.ascona.util.dis;

import java.io.Serializable;

import ch.alpine.ascona.util.ren.EmptyRender;
import ch.alpine.ascona.util.ren.RenderInterface;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.sophus.crv.clt.ClothoidBuilder;
import ch.alpine.sophus.crv.d2.Arrowhead;
import ch.alpine.sophus.decim.LineDistance;
import ch.alpine.sophus.math.sample.RandomSampleInterface;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.TensorUnaryOperator;

// TODO ASCONA ALG probably obsolete: instead use Se2 and Se2Covering with different clothoid builders
public abstract class AbstractClothoidDisplay implements ManifoldDisplay, Serializable {
  private static final Tensor SPEARHEAD = Arrowhead.of(0.2).unmodifiable();
  // PolygonNormalize.of( //
  // Spearhead.of(Tensors.vector(-0.217, -0.183, 4.189), RealScalar.of(0.1)), RealScalar.of(0.08));
  // private static final Tensor ARROWHEAD = Arrowhead.of(0.2).unmodifiable();

  @Override
  public abstract ClothoidBuilder geodesicSpace();

  @Override // from ManifoldDisplay
  public final int dimensions() {
    return 3;
  }

  @Override // from ManifoldDisplay
  public final Tensor shape() {
    return SPEARHEAD;
  }

  @Override // from ManifoldDisplay
  public final TensorUnaryOperator tangentProjection(Tensor p) {
    return v -> v.extract(0, 2);
  }

  @Override
  public final Tensor point2xya(Tensor p) {
    return p.copy();
  }

  @Override // from ManifoldDisplay
  public final Tensor matrixLift(Tensor p) {
    return GfxMatrix.of(p);
  }

  @Override // from ManifoldDisplay
  public final LineDistance lineDistance() {
    return null;
  }

  @Override // from ManifoldDisplay
  public final RandomSampleInterface randomSampleInterface() {
    return null;
  }

  @Override // from ManifoldDisplay
  public final RenderInterface background() {
    return EmptyRender.INSTANCE;
  }

  @Override // from Object
  public abstract String toString();
}
