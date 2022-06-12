// code by jph
package ch.alpine.ascona.util.dis;

import ch.alpine.ascona.util.ren.EmptyRender;
import ch.alpine.ascona.util.win.RenderInterface;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.sophus.api.TensorMetric;
import ch.alpine.sophus.decim.LineDistance;
import ch.alpine.sophus.hs.Biinvariant;
import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.sophus.lie.dt.DtGroup;
import ch.alpine.sophus.lie.dt.DtRandomSample;
import ch.alpine.sophus.math.sample.RandomSampleInterface;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.VectorQ;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.lie.r2.CirclePoints;
import ch.alpine.tensor.pdf.c.UniformDistribution;
import ch.alpine.tensor.red.Max;

public enum Dt1Display implements ManifoldDisplay {
  INSTANCE;

  private static final Tensor PENTAGON = CirclePoints.of(5).multiply(RealScalar.of(0.2)).unmodifiable();
  // Fehlerhaft, aber zurzeit Probleme mit Ausnahme bei lambda = 0
  private static final ScalarUnaryOperator MAX_X = Max.function(RealScalar.of(0.001));

  @Override // from ManifoldDisplay
  public int dimensions() {
    return 2;
  }

  @Override // from ManifoldDisplay
  public TensorUnaryOperator tangentProjection(Tensor p) {
    return null;
  }

  @Override // from ManifoldDisplay
  public Tensor shape() {
    return PENTAGON;
  }

  @Override // from ManifoldDisplay
  public Tensor project(Tensor xya) {
    Tensor point = xya.extract(0, 2);
    point.set(MAX_X, 0);
    return point;
  }

  @Override // from ManifoldDisplay
  public Tensor toPoint(Tensor p) {
    return VectorQ.requireLength(p, 2);
  }

  @Override // from ManifoldDisplay
  public Tensor matrixLift(Tensor p) {
    return GfxMatrix.translation(p);
  }

  @Override
  public GeodesicSpace geodesicSpace() {
    return DtGroup.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public TensorMetric biinvariantMetric() {
    return null;
  }

  @Override // from ManifoldDisplay
  public Biinvariant biinvariant() {
    return null;
  }

  @Override // from ManifoldDisplay
  public LineDistance lineDistance() {
    return null;
  }

  @Override // from ManifoldDisplay
  public RandomSampleInterface randomSampleInterface() {
    return new DtRandomSample(1, UniformDistribution.unit(), UniformDistribution.of(-1, 1));
  }

  @Override
  public Tensor unproject(Tensor p) {
    return p.copy().append(RealScalar.ZERO);
  }

  @Override // from ManifoldDisplay
  public RenderInterface background() {
    return EmptyRender.INSTANCE;
  }

  @Override // from Object
  public String toString() {
    return "Dt1";
  }
}
