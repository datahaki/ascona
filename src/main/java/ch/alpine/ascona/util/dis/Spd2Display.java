// code by jph
package ch.alpine.ascona.util.dis;

import ch.alpine.ascona.util.ren.EmptyRender;
import ch.alpine.ascona.util.ren.RenderInterface;
import ch.alpine.sophus.decim.LineDistance;
import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.sophus.hs.spd.Spd0Exponential;
import ch.alpine.sophus.hs.spd.SpdManifold;
import ch.alpine.sophus.hs.spd.TSpdRandomSample;
import ch.alpine.sophus.math.sample.RandomSampleInterface;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.PadRight;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.lie.r2.CirclePoints;
import ch.alpine.tensor.mat.DiagonalMatrix;
import ch.alpine.tensor.pdf.c.UniformDistribution;
import ch.alpine.tensor.red.Diagonal;

/** symmetric positive definite 2 x 2 matrices */
public enum Spd2Display implements ManifoldDisplay {
  INSTANCE;

  // TODO ASCONA make as scale == 1 and zoom instead, or justify
  private static final Scalar SCALE = RealScalar.of(0.2);
  private static final Tensor CIRCLE_POINTS = CirclePoints.of(43).multiply(SCALE).unmodifiable();
  private static final TensorUnaryOperator PAD_RIGHT = PadRight.zeros(3, 3);

  @Override // from ManifoldDisplay
  public int dimensions() {
    return 3;
  }

  @Override // from ManifoldDisplay
  public TensorUnaryOperator tangentProjection(Tensor p) {
    return null;
  }

  @Override // from ManifoldDisplay
  public Tensor shape() {
    return CIRCLE_POINTS;
  }

  private static Tensor xya2sim(Tensor xya) {
    xya = xya.multiply(SCALE);
    Tensor sim = DiagonalMatrix.with(xya.extract(0, 2));
    sim.set(xya.Get(2), 0, 1);
    sim.set(xya.Get(2), 1, 0);
    return sim;
  }

  private static Tensor sim2xya(Tensor sim) {
    return Diagonal.of(sim).append(sim.get(0, 1)).divide(SCALE);
  }

  @Override // from ManifoldDisplay
  public Tensor xya2point(Tensor xya) {
    Tensor sim = xya2sim(xya);
    return Spd0Exponential.INSTANCE.exp(sim);
  }

  @Override // from ManifoldDisplay
  public Tensor point2xya(Tensor p) {
    return sim2xya(Spd0Exponential.INSTANCE.log(p));
  }

  @Override // from ManifoldDisplay
  public Tensor matrixLift(Tensor sym) {
    Tensor matrix = PAD_RIGHT.apply(sym); // log is possible
    matrix.set(RealScalar.ONE, 2, 2);
    Tensor xy = point2xy(sym);
    matrix.set(xy.Get(0), 0, 2);
    matrix.set(xy.Get(1), 1, 2);
    return matrix;
  }

  @Override
  public GeodesicSpace geodesicSpace() {
    return SpdManifold.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public LineDistance lineDistance() {
    return null;
  }

  @Override // from ManifoldDisplay
  public RandomSampleInterface randomSampleInterface() {
    TSpdRandomSample tSpdRandomSample = new TSpdRandomSample(2, UniformDistribution.of(-1, 1));
    return random -> Spd0Exponential.INSTANCE.exp(tSpdRandomSample.randomSample(random));
  }

  @Override // from ManifoldDisplay
  public RenderInterface background() {
    return EmptyRender.INSTANCE;
  }
}
