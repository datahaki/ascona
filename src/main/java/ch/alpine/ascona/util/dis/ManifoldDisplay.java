// code by jph
package ch.alpine.ascona.util.dis;

import ch.alpine.ascona.util.arp.HsArrayPlot;
import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.sophus.api.TensorMetric;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.decim.LineDistance;
import ch.alpine.sophus.hs.Biinvariant;
import ch.alpine.sophus.hs.HsManifold;
import ch.alpine.sophus.hs.HsTransport;
import ch.alpine.sophus.lie.LieGroup;
import ch.alpine.sophus.math.sample.RandomSampleInterface;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.TensorUnaryOperator;

/** Hint: the interface is intended for use in the demo layer
 * but not in the library functions. */
public interface ManifoldDisplay {
  /** @return dimensions of the manifold, strictly positive */
  int dimensions();

  /** @return polygon to visualize the control point */
  Tensor shape();

  /** @param xya vector of length 3
   * @return control point */
  Tensor project(Tensor xya);

  /** @param p control point
   * @return vector of length 2 with grid coordinates {x, y} */
  Tensor toPoint(Tensor p);

  /** @param p control point
   * @return matrix with dimensions 3 x 3 */
  Tensor matrixLift(Tensor p);

  /** @return never null */
  GeodesicSpace geodesicSpace();

  // ---
  /** TODO ASCONA API define guarantees, at the moment null for:
   * ClA
   * Cl3
   * ClC
   * R2S1 A
   * R2S1 B
   * Spd2
   * S1
   * S2
   * H1
   * H2
   * 
   * @return lie group if the space is a lie group, or null if function is not applicable */
  LieGroup lieGroup();

  HsManifold hsManifold();

  /** @param p
   * @return operator that maps arbitrary dimension tangent vectors to 2d for display */
  TensorUnaryOperator tangentProjection(Tensor p);

  HsTransport hsTransport();

  /** FIXME ASCONA API define guarantees, at the moment null for:
   * R2S1 A
   * R2S1 B
   * He1
   * Dt1
   * 
   * @param p control point
   * @param q control point
   * @return (pseudo-) distance between given control points p and q
   * or if functionality is not supported */
  TensorMetric parametricDistance();

  /** FIXME ASCONA API define guarantees, at the moment null for:
   * ClA
   * Cl3
   * ClC
   * SE2C
   * R2S1 A
   * R2S1 B
   * SE2
   * He1
   * Dt1
   * 
   * @return metric biinvariant or null if metric is not biinvariant */
  Biinvariant biinvariant();

  /** FIXME ASCONA API define guarantees, at the moment null for:
   * ClA
   * Cl3
   * ClC
   * R2S1 A
   * R2S1 B
   * 
   * @return biinvariantMean, or null, if geodesic space does not support the computation of an biinvariant mean */
  BiinvariantMean biinvariantMean();

  LineDistance lineDistance();

  /** @param resolution
   * @param tensorScalarFunction
   * @return array of scalar values clipped to interval [0, 1] or DoubleScalar.INDETERMINATE */
  HsArrayPlot geodesicArrayPlot();

  RandomSampleInterface randomSampleInterface();

  @Override // from Object
  String toString();
}
