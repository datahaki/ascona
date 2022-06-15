// code by jph
package ch.alpine.ascona.util.dis;

import ch.alpine.ascona.util.win.RenderInterface;
import ch.alpine.sophus.api.TensorMetric;
import ch.alpine.sophus.decim.LineDistance;
import ch.alpine.sophus.hs.Biinvariant;
import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.sophus.hs.HomogeneousSpace;
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

  /** inverse to project function
   * 
   * @param p
   * @return xya */
  Tensor unproject(Tensor p);

  /** @param p control point
   * @return vector of length 2 with grid coordinates {x, y} */
  Tensor toPoint(Tensor p);

  /** @param p control point
   * @return matrix with dimensions 3 x 3 */
  Tensor matrixLift(Tensor p);

  /** @return never null
   * @see HomogeneousSpace
   * @see LieGroup */
  GeodesicSpace geodesicSpace();

  /** @param p
   * @return operator that maps arbitrary dimension tangent vectors to 2d for display */
  TensorUnaryOperator tangentProjection(Tensor p);

  /** @return distance operator that gives distance between two points p and q
   * that is invariant under all symmetries, or null if operator does not exist */
  TensorMetric biinvariantMetric();

  /** ClA
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
  // FIXME ASCONA API unacceptable
  Biinvariant biinvariant();

  LineDistance lineDistance();

  /** @return points in manifold that have to be {@link #unproject(Tensor)}ed
   * in order to become control points in the form xya */
  RandomSampleInterface randomSampleInterface();

  /** @return rendering of background, for instance a shaded sphere for S^2 */
  RenderInterface background();

  @Override // from Object
  String toString();
}
