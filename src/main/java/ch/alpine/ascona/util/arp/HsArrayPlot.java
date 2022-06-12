// code by jph
package ch.alpine.ascona.util.arp;

import java.util.Optional;
import java.util.function.Function;

import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.N;

public interface HsArrayPlot {
  /** @param hsArrayPlot
   * @param resolution
   * @param arrayFunction
   * @return */
  static <T extends Tensor> Tensor of(HsArrayPlot hsArrayPlot, int resolution, ArrayFunction<T> arrayFunction) {
    CoordinateBoundingBox coordinateBoundingBox = hsArrayPlot.coordinateBoundingBox();
    Tensor dx = Subdivide.increasing(coordinateBoundingBox.getClip(0), resolution - 1).map(N.DOUBLE);
    Tensor dy = Subdivide.decreasing(coordinateBoundingBox.getClip(1), resolution - 1).map(N.DOUBLE);
    Function<Tensor, T> function = arrayFunction.function();
    T fallback = arrayFunction.fallback();
    return Tensor.of(dy.stream().parallel() //
        .map(py -> Tensor.of(dx.stream() //
            .map(px -> Tensors.of(px, py)) // in R2
            .map(hsArrayPlot::raster) //
            .map(optional -> optional.map(function).orElse(fallback)))));
  }

  /** @param pxy vector of the form {px, py}
   * @return */
  Optional<Tensor> raster(Tensor pxy);

  /** @return 2-dimensional bounding box to sample within */
  CoordinateBoundingBox coordinateBoundingBox();
}
