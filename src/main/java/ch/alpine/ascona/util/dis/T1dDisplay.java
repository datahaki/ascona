// code by jph
package ch.alpine.ascona.util.dis;

import java.util.Optional;

import ch.alpine.ascona.util.api.Box2D;
import ch.alpine.ascona.util.arp.D2Raster;
import ch.alpine.ascona.util.ren.AxesRender;
import ch.alpine.ascona.util.win.RenderInterface;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.sophus.decim.LineDistance;
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
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.pdf.c.ExponentialDistribution;
import ch.alpine.tensor.pdf.c.UniformDistribution;
import ch.alpine.tensor.sca.Clips;
import ch.alpine.tensor.sca.exp.Exp;
import ch.alpine.tensor.sca.exp.Log;

public enum T1dDisplay implements ManifoldDisplay, D2Raster {
  INSTANCE;

  private static final Tensor PENTAGON = CirclePoints.of(5).multiply(RealScalar.of(0.1)).unmodifiable();

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
  public Tensor xya2point(Tensor xya) {
    Tensor point = xya.extract(0, 2);
    point.set(Exp.FUNCTION, 1);
    return point;
  }

  @Override // from ManifoldDisplay
  public Tensor point2xya(Tensor p) {
    return Append.of(point2xy(p), RealScalar.ZERO);
  }

  @Override // from ManifoldDisplay
  public Tensor point2xy(Tensor p) {
    Tensor q = VectorQ.requireLength(p, 2).copy();
    q.set(Log.FUNCTION, 1);
    return q;
  }

  @Override // from ManifoldDisplay
  public Tensor matrixLift(Tensor p) {
    // Scalar f = Exp.FUNCTION.apply(p.Get(1).subtract(RealScalar.of(10)));
    // Tensor diag = DiagonalMatrix.of(f, f, RealScalar.ONE);
    return GfxMatrix.translation(point2xy(p)); // .dot(diag);
  }

  @Override // from ManifoldDisplay
  public GeodesicSpace geodesicSpace() {
    return TdGroup.INSTANCE;
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

  @Override // D2Raster
  public Optional<Tensor> d2lift(Tensor pxy) {
    return Optional.of(xya2point(pxy));
  }

  @Override // D2Raster
  public CoordinateBoundingBox coordinateBoundingBox() {
    return Box2D.xy(Clips.absolute(3));
  }
}
