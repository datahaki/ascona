// code by jph
package ch.alpine.ascona.util.dis;

import java.io.Serializable;

import ch.alpine.ascona.util.arp.HsArrayPlot;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.sophus.api.TensorMetric;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.crv.d2.Arrowhead;
import ch.alpine.sophus.decim.LineDistance;
import ch.alpine.sophus.hs.Biinvariant;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.lie.LieGroup;
import ch.alpine.sophus.lie.so2.So2;
import ch.alpine.sophus.math.sample.RandomSampleInterface;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.TensorUnaryOperator;

public abstract class R2S1AbstractDisplay implements ManifoldDisplay, Serializable {
  private static final Tensor ARROWHEAD = Arrowhead.of(0.2).unmodifiable();

  @Override // from ManifoldDisplay
  public final int dimensions() {
    return 3;
  }

  @Override // from ManifoldDisplay
  public final Tensor shape() {
    return ARROWHEAD;
  }

  @Override // from ManifoldDisplay
  public final Tensor project(Tensor xya) {
    Tensor xym = xya.copy();
    xym.set(So2.MOD, 2);
    return xym;
  }

  @Override // from ManifoldDisplay
  public final TensorUnaryOperator tangentProjection(Tensor xyz) {
    return null;
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
  public final LieGroup lieGroup() {
    return null;
  }

  @Override // from ManifoldDisplay
  public final HomogeneousSpace homogeneousSpace() {
    return null;
  }

  @Override // from ManifoldDisplay
  public final TensorMetric parametricDistance() {
    return null;
  }

  @Override // from ManifoldDisplay
  public final Biinvariant biinvariant() {
    return null;
  }

  @Override // from ManifoldDisplay
  public final BiinvariantMean biinvariantMean() {
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

  @Override // from ManifoldDisplay
  public final RandomSampleInterface randomSampleInterface() {
    return null;
  }
}
