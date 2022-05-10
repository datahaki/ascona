// code by jph
package ch.alpine.ascona.util.dis;

import java.io.Serializable;

import ch.alpine.ascona.util.arp.HsArrayPlot;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.sophus.api.TensorMetric;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.decim.LineDistance;
import ch.alpine.sophus.hs.Biinvariant;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.hs.MetricBiinvariant;
import ch.alpine.sophus.lie.LieGroup;
import ch.alpine.sophus.lie.so3.Rodrigues;
import ch.alpine.sophus.lie.so3.So3BiinvariantMean;
import ch.alpine.sophus.lie.so3.So3Geodesic;
import ch.alpine.sophus.lie.so3.So3Group;
import ch.alpine.sophus.lie.so3.So3Metric;
import ch.alpine.sophus.math.sample.RandomSampleInterface;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.lie.r2.CirclePoints;
import ch.alpine.tensor.nrm.Vector2Norm;

/** symmetric positive definite 2 x 2 matrices */
public class So3Display implements ManifoldDisplay, Serializable {
  private static final Tensor TRIANGLE = CirclePoints.of(3).multiply(RealScalar.of(0.4)).unmodifiable();
  private static final Scalar RADIUS = RealScalar.of(7);
  // ---
  public static final ManifoldDisplay INSTANCE = new So3Display(RADIUS);
  // ---
  private final Scalar radius;

  public So3Display(Scalar radius) {
    this.radius = radius;
  }

  @Override // from ManifoldDisplay
  public int dimensions() {
    return 3;
  }

  @Override // from ManifoldDisplay
  public GeodesicSpace geodesicSpace() {
    return So3Geodesic.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public Tensor shape() {
    return TRIANGLE;
  }

  @Override // from ManifoldDisplay
  public Tensor project(Tensor xya) {
    Tensor axis = xya.divide(radius);
    Scalar norm = Vector2Norm.of(axis);
    if (Scalars.lessThan(RealScalar.ONE, norm))
      axis = axis.divide(norm);
    return Rodrigues.vectorExp(axis);
  }

  @Override // from ManifoldDisplay
  public final TensorUnaryOperator tangentProjection(Tensor xyz) {
    return null;
  }

  @Override // from ManifoldDisplay
  public Tensor toPoint(Tensor xyz) {
    return Rodrigues.INSTANCE.vectorLog(xyz).extract(0, 2).multiply(radius);
  }

  @Override // from ManifoldDisplay
  public Tensor matrixLift(Tensor xyz) {
    return GfxMatrix.translation(toPoint(xyz));
  }

  @Override // from ManifoldDisplay
  public LieGroup lieGroup() {
    return So3Group.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public HomogeneousSpace hsManifold() {
    return So3Group.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public TensorMetric parametricDistance() {
    return So3Metric.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public Biinvariant biinvariant() {
    return MetricBiinvariant.EUCLIDEAN;
  }

  @Override // from ManifoldDisplay
  public BiinvariantMean biinvariantMean() {
    return So3BiinvariantMean.INSTANCE;
  }

  @Override
  public final LineDistance lineDistance() {
    return null; // TODO ASCONA ALG line distance should be similar to s^3
  }

  @Override
  public HsArrayPlot geodesicArrayPlot() {
    return null;
  }

  @Override
  public RandomSampleInterface randomSampleInterface() {
    return null;
  }

  @Override // from Object
  public String toString() {
    return "SO3";
  }
}
