// code by jph
package ch.alpine.ascona.util.dis;

import ch.alpine.ascona.util.ren.RenderInterface;
import ch.alpine.sophus.crv.TransitionSpace;
import ch.alpine.sophus.crv.UniformTransitionSpace;
import ch.alpine.sophus.decim.LineDistance;
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
  Tensor xya2point(Tensor xya);

  /** pseudo-inverse to xya2point function
   * 
   * @param p
   * @return xya */
  Tensor point2xya(Tensor p);

  /** function is for drawing paths
   * 
   * @param p control point
   * @return vector of length 2 with grid coordinates {x, y} */
  default Tensor point2xy(Tensor p) {
    return point2xya(p).extract(0, 2);
  }

  /** function is for drawing control points with proper orientation
   * 
   * @param p control point
   * @return matrix with dimensions 3 x 3 */
  Tensor matrixLift(Tensor p);

  /** @return never null
   * @see HomogeneousSpace
   * @see LieGroup */
  GeodesicSpace geodesicSpace();

  default TransitionSpace transitionSpace() {
    return new UniformTransitionSpace(geodesicSpace());
  }

  /** @param p
   * @return operator that maps arbitrary dimension tangent vectors to 2d for display */
  TensorUnaryOperator tangentProjection(Tensor p);

  LineDistance lineDistance();

  /** @return points in manifold that have to be {@link #point2xya(Tensor)}ed
   * in order to become control points in the form xya */
  RandomSampleInterface randomSampleInterface();

  /** @return rendering of background, for instance a shaded sphere for S^2 */
  RenderInterface background();
}
