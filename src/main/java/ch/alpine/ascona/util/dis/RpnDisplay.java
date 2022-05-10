// code by jph
package ch.alpine.ascona.util.dis;

import java.io.Serializable;

import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.sophus.api.TensorMetric;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.decim.LineDistance;
import ch.alpine.sophus.hs.Biinvariant;
import ch.alpine.sophus.hs.HsManifold;
import ch.alpine.sophus.hs.MetricBiinvariant;
import ch.alpine.sophus.hs.rpn.RpnManifold;
import ch.alpine.sophus.hs.rpn.RpnMetric;
import ch.alpine.sophus.hs.rpn.RpnRandomSample;
import ch.alpine.sophus.hs.sn.SnFastMean;
import ch.alpine.sophus.hs.sn.SnGeodesic;
import ch.alpine.sophus.lie.LieGroup;
import ch.alpine.sophus.math.sample.RandomSampleInterface;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.lie.r2.CirclePoints;

/** symmetric positive definite 2 x 2 matrices */
public abstract class RpnDisplay implements ManifoldDisplay, Serializable {
  private static final Tensor CIRCLE = CirclePoints.of(15).multiply(RealScalar.of(0.05)).unmodifiable();
  // ---
  private final int dimensions;

  protected RpnDisplay(int dimensions) {
    this.dimensions = dimensions;
  }

  @Override // from ManifoldDisplay
  public final GeodesicSpace geodesicSpace() {
    return SnGeodesic.INSTANCE; // TODO ASCONA ALG
  }

  @Override // from ManifoldDisplay
  public final int dimensions() {
    return dimensions;
  }

  @Override // from ManifoldDisplay
  public final Tensor shape() {
    return CIRCLE;
  }

  @Override // from ManifoldDisplay
  public final LieGroup lieGroup() {
    return null;
  }

  @Override // from ManifoldDisplay
  public final HsManifold hsManifold() {
    return RpnManifold.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public final TensorMetric parametricDistance() {
    return RpnMetric.INSTANCE;
  }

  @Override // from ManifoldDisplay
  public final Biinvariant biinvariant() {
    return MetricBiinvariant.EUCLIDEAN;
  }

  @Override // from ManifoldDisplay
  public final BiinvariantMean biinvariantMean() {
    return SnFastMean.INSTANCE; // TODO ASCONA ALG
  }

  @Override // from ManifoldDisplay
  public final LineDistance lineDistance() {
    return null;
  }

  @Override // from ManifoldDisplay
  public final RandomSampleInterface randomSampleInterface() {
    return RpnRandomSample.of(dimensions());
  }

  @Override
  public final String toString() {
    return "RP" + dimensions();
  }
}
