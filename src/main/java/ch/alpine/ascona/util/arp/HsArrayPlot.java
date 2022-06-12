// code by jph
package ch.alpine.ascona.util.arp;

import java.util.Optional;

import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;

public interface HsArrayPlot {
  /** @param pxy vector of the form {px, py}
   * @return */
  Optional<Tensor> raster(Tensor pxy);

  /** @return 2-dimensional bounding box to sample within */
  CoordinateBoundingBox coordinateBoundingBox();
}
