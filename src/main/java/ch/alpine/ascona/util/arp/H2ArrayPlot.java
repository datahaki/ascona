// code by jph
package ch.alpine.ascona.util.arp;

import java.io.Serializable;
import java.util.function.Function;

import ch.alpine.sophus.hs.hn.HnWeierstrassCoordinate;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;

public record H2ArrayPlot(CoordinateBoundingBox coordinateBoundingBox) implements HsArrayPlot, Serializable {
  @Override // from HsArrayPlot
  public Tensor raster(int resolution, Function<Tensor, ? extends Tensor> function, Tensor fallback) {
    Tensor dx = Subdivide.increasing(coordinateBoundingBox.getClip(0), resolution - 1);
    Tensor dy = Subdivide.decreasing(coordinateBoundingBox.getClip(1), resolution - 1);
    return Tensor.of(dy.stream().parallel() //
        .map(py -> Tensor.of(dx.stream() //
            .map(px -> Tensors.of(px, py)) //
            .map(HnWeierstrassCoordinate::toPoint) //
            .map(function))));
  }
}
