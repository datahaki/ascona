// code by jph
package ch.alpine.ascona.util.arp;

import java.util.List;
import java.util.Optional;

import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.N;

public interface D2Raster {
  /** @param d2Raster
   * @param resolution
   * @param arrayFunction
   * @return */
  static <T extends Tensor> Tensor of(D2Raster d2Raster, int resolution, ArrayFunction<T> arrayFunction) {
    CoordinateBoundingBox coordinateBoundingBox = d2Raster.coordinateBoundingBox();
    Tensor dx = Subdivide.increasing(coordinateBoundingBox.getClip(0), resolution - 1).map(N.DOUBLE);
    Tensor dy = Subdivide.decreasing(coordinateBoundingBox.getClip(1), resolution - 1).map(N.DOUBLE);
    return Tensor.of(dy.stream().map(Scalar.class::cast).parallel() //
        .map(py -> Tensor.of(dx.stream().map(Scalar.class::cast) //
            .map(px -> Unprotect.using(List.of(px, py))) //
            .map(d2Raster::d2lift) //
            .map(arrayFunction))));
  }

  /** in some simple cases the implementation is just
   * Optional.of(project(pxy))
   * 
   * @param pxy vector of the form {px, py}
   * @return point on manifold, or empty */
  Optional<Tensor> d2lift(Tensor pxy);

  /** @return 2-dimensional bounding box to sample within */
  CoordinateBoundingBox coordinateBoundingBox();
}
