// code by jph
package ch.alpine.ascona.util.dis;

import ch.alpine.ascona.util.ren.EmptyRender;
import ch.alpine.ascona.util.win.RenderInterface;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.sophus.api.TensorMetric;
import ch.alpine.sophus.decim.LineDistance;
import ch.alpine.sophus.hs.Biinvariant;
import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.sophus.lie.he.HeGroup;
import ch.alpine.sophus.lie.he.HeRandomSample;
import ch.alpine.sophus.math.sample.RandomSampleInterface;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.VectorQ;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.lie.r2.CirclePoints;
import ch.alpine.tensor.pdf.c.UniformDistribution;

public enum He1Display implements ManifoldDisplay {
  INSTANCE;

  private static final Tensor SQUARE = CirclePoints.of(4).multiply(RealScalar.of(0.2)).unmodifiable();

  @Override // from ManifoldDisplay
  public int dimensions() {
    return 3;
  }

  @Override // from ManifoldDisplay
  public Tensor shape() {
    return SQUARE;
  }

  @Override // from ManifoldDisplay
  public Tensor project(Tensor xya) {
    return Tensors.of(xya.extract(0, 1), xya.extract(1, 2), xya.Get(2));
  }

  @Override // from ManifoldDisplay
  public final TensorUnaryOperator tangentProjection(Tensor xyz) {
    return null;
  }

  @Override // from ManifoldDisplay
  public Tensor toPoint(Tensor p) {
    if (VectorQ.of(p))
      throw new RuntimeException();
    return Tensors.of(p.Get(0, 0), p.Get(1, 0));
  }

  @Override // from ManifoldDisplay
  public Tensor matrixLift(Tensor p) {
    return GfxMatrix.translation(toPoint(p));
  }

  @Override
  public GeodesicSpace geodesicSpace() {
    return HeGroup.INSTANCE;
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
    return new HeRandomSample(1, UniformDistribution.of(-1, 1));
  }

  @Override // from ManifoldDisplay
  public Tensor lift(Tensor p) {
    return Tensors.of(p.Get(0, 0), p.Get(1, 0), p.Get(2));
  }

  @Override // from ManifoldDisplay
  public RenderInterface background() {
    return EmptyRender.INSTANCE;
  }

  @Override // from Object
  public String toString() {
    return "He1";
  }
}
