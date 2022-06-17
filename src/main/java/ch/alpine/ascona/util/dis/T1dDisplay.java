// code by jph
package ch.alpine.ascona.util.dis;

import ch.alpine.ascona.util.ren.AxesRender;
import ch.alpine.ascona.util.win.RenderInterface;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.sophus.decim.LineDistance;
import ch.alpine.sophus.hs.Biinvariant;
import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.sophus.lie.td.TdGroup;
import ch.alpine.sophus.lie.td.TdRandomSample;
import ch.alpine.sophus.math.sample.RandomSampleInterface;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Append;
import ch.alpine.tensor.alg.VectorQ;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.lie.r2.CirclePoints;
import ch.alpine.tensor.pdf.c.ExponentialDistribution;
import ch.alpine.tensor.pdf.c.UniformDistribution;
import ch.alpine.tensor.sca.exp.Exp;
import ch.alpine.tensor.sca.exp.Log;

public enum T1dDisplay implements ManifoldDisplay {
  INSTANCE;

  private static final Tensor PENTAGON = CirclePoints.of(5).multiply(RealScalar.of(0.2)).unmodifiable();

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
    point.set(Exp.FUNCTION, 1);
    return point;
  }

  @Override // from ManifoldDisplay
  public Tensor unproject(Tensor p) {
    return Append.of(toPoint(p), RealScalar.ZERO);
  }

  @Override // from ManifoldDisplay
  public Tensor toPoint(Tensor p) {
    Tensor q = VectorQ.requireLength(p, 2).copy();
    q.set(Log.FUNCTION, 1);
    return q;
  }

  @Override // from ManifoldDisplay
  public Tensor matrixLift(Tensor p) {
    return GfxMatrix.translation(toPoint(p));
  }

  @Override // from ManifoldDisplay
  public GeodesicSpace geodesicSpace() {
    return TdGroup.INSTANCE;
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
    return new TdRandomSample(UniformDistribution.of(-3, 3), 1, ExponentialDistribution.standard());
  }

  @Override // from ManifoldDisplay
  public RenderInterface background() {
    return AxesRender.INSTANCE;
  }

  @Override // from Object
  public String toString() {
    return "T1d";
  }
}
