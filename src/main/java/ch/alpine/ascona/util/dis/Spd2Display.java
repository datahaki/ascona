// code by jph
package ch.alpine.ascona.util.dis;

import ch.alpine.ascona.util.arp.HsArrayPlot;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.sophus.api.TensorMetric;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.decim.LineDistance;
import ch.alpine.sophus.hs.Biinvariant;
import ch.alpine.sophus.hs.HsManifold;
import ch.alpine.sophus.hs.MetricBiinvariant;
import ch.alpine.sophus.hs.spd.Spd0Exponential;
import ch.alpine.sophus.hs.spd.SpdBiinvariantMean;
import ch.alpine.sophus.hs.spd.SpdManifold;
import ch.alpine.sophus.hs.spd.SpdMetric;
import ch.alpine.sophus.lie.LieGroup;
import ch.alpine.sophus.math.sample.RandomSampleInterface;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.PadRight;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.lie.r2.CirclePoints;
import ch.alpine.tensor.mat.DiagonalMatrix;
import ch.alpine.tensor.red.Diagonal;

/** symmetric positive definite 2 x 2 matrices */
public enum Spd2Display implements ManifoldDisplay {
  INSTANCE;

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
  public Tensor project(Tensor xya) {
    Tensor sim = xya2sim(xya);
    return Spd0Exponential.INSTANCE.exp(sim);
  }

  @Override // from ManifoldDisplay
  public Tensor toPoint(Tensor sym) {
    Tensor sim = Spd0Exponential.INSTANCE.log(sym);
    return sim2xya(sim).extract(0, 2);
  }

  @Override // from ManifoldDisplay
  public Tensor matrixLift(Tensor sym) {
    Tensor matrix = PAD_RIGHT.apply(sym); // log is possible
    matrix.set(RealScalar.ONE, 2, 2);
    return GfxMatrix.translation(toPoint(sym)).dot(matrix);
  }

  @Override // from ManifoldDisplay
  public LieGroup lieGroup() {
    return null;
  }

  @Override // from ManifoldDisplay
  public HsManifold hsManifold() {
    return SpdManifold.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public TensorMetric parametricDistance() {
    return SpdMetric.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public Biinvariant biinvariant() {
    return MetricBiinvariant.VECTORIZE0;
  }

  @Override // from ManifoldDisplay
  public BiinvariantMean biinvariantMean() {
    return SpdBiinvariantMean.INSTANCE;
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
    return "Spd2";
  }
}
