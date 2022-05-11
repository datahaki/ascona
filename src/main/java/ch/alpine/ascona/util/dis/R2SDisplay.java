// code by jph
package ch.alpine.ascona.util.dis;

import ch.alpine.ascona.util.arp.HsArrayPlot;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.sophus.api.TensorMetric;
import ch.alpine.sophus.crv.d2.Arrowhead;
import ch.alpine.sophus.decim.LineDistance;
import ch.alpine.sophus.hs.Biinvariant;
import ch.alpine.sophus.hs.MetricBiinvariant;
import ch.alpine.sophus.lie.r2s.R2SGroup;
import ch.alpine.sophus.lie.so2.So2;
import ch.alpine.sophus.math.sample.RandomSampleInterface;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.TensorUnaryOperator;

public enum R2SDisplay implements ManifoldDisplay {
  INSTANCE;

  private static final Tensor ARROWHEAD = Arrowhead.of(0.2).unmodifiable();

  @Override // from ManifoldDisplay
  public int dimensions() {
    return 3;
  }

  @Override // from ManifoldDisplay
  public Tensor shape() {
    return ARROWHEAD;
  }

  @Override // from ManifoldDisplay
  public Tensor project(Tensor xya) {
    Tensor xym = xya.copy();
    xym.set(So2.MOD, 2);
    return xym;
  }

  @Override // from ManifoldDisplay
  public final TensorUnaryOperator tangentProjection(Tensor xyz) {
    return null;
  }

  @Override // from ManifoldDisplay
  public Tensor toPoint(Tensor p) {
    return p.extract(0, 2);
  }

  @Override // from ManifoldDisplay
  public Tensor matrixLift(Tensor p) {
    return GfxMatrix.of(p);
  }

  @Override
  public GeodesicSpace geodesicSpace() {
    return R2SGroup.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public TensorMetric biinvariantMetric() {
    return null;
  }

  @Override // from ManifoldDisplay
  public Biinvariant biinvariant() {
    return MetricBiinvariant.EUCLIDEAN;
  }

  @Override // from ManifoldDisplay
  public LineDistance lineDistance() {
    return null;
  }

  @Override // from ManifoldDisplay
  public HsArrayPlot geodesicArrayPlot() {
    return null;
  }

  @Override // from ManifoldDisplay
  public RandomSampleInterface randomSampleInterface() {
    return null;
  }

  @Override // from Object
  public String toString() {
    return "R2S";
  }
}
