// code by jph
package ch.alpine.ascona.util.dis;

import java.io.Serializable;

import ch.alpine.ascona.util.arp.HsArrayPlot;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.sophus.api.TensorMetric;
import ch.alpine.sophus.crv.d2.Arrowhead;
import ch.alpine.sophus.decim.LineDistance;
import ch.alpine.sophus.hs.Biinvariant;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.TensorUnaryOperator;

public abstract class Se2AbstractDisplay implements ManifoldDisplay, Serializable {
  private static final Tensor ARROWHEAD = Arrowhead.of(0.2).unmodifiable();

  @Override // from ManifoldDisplay
  public final int dimensions() {
    return 3;
  }

  @Override // from ManifoldDisplay
  public final TensorUnaryOperator tangentProjection(Tensor p) {
    return v -> v.extract(0, 2);
  }

  @Override // from ManifoldDisplay
  public final Tensor shape() {
    return ARROWHEAD;
  }

  @Override // from ManifoldDisplay
  public final Tensor toPoint(Tensor p) {
    return p.extract(0, 2);
  }

  @Override // from ManifoldDisplay
  public final Tensor matrixLift(Tensor p) {
    return GfxMatrix.of(p);
  }

  @Override // from ManifoldDisplay
  public final TensorMetric biinvariantMetric() {
    return null;
  }

  @Override // from ManifoldDisplay
  public final Biinvariant biinvariant() {
    return null;
  }

  @Override // from ManifoldDisplay
  public final LineDistance lineDistance() {
    return null;
  }

  @Override // from ManifoldDisplay
  public final HsArrayPlot geodesicArrayPlot() {
    return null;
  }

  @Override // from Object
  public abstract String toString();
}
