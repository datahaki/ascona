// code by jph
package ch.alpine.ascona.util.arp;

import java.awt.Dimension;
import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;

public class R2ArrayPlot implements HsArrayPlot, Serializable {
  public static HsArrayPlot of(Scalar radius) {
    Clip clip = Clips.absolute(radius);
    return new R2ArrayPlot(CoordinateBoundingBox.of(Stream.generate(() -> clip).limit(2)));
  }

  private final CoordinateBoundingBox coordinateBoundingBox;

  public R2ArrayPlot(CoordinateBoundingBox coordinateBoundingBox) {
    this.coordinateBoundingBox = Objects.requireNonNull(coordinateBoundingBox);
  }

  @Override // from GeodesicArrayPlot
  public Tensor raster(int resolution, Function<Tensor, ? extends Tensor> function, Tensor fallback) {
    Tensor dx = Subdivide.increasing(coordinateBoundingBox.getClip(0), resolution - 1);
    Tensor dy = Subdivide.decreasing(coordinateBoundingBox.getClip(1), resolution - 1);
    return Tensor.of(dy.stream().parallel() //
        .map(py -> Tensor.of(dx.stream() //
            .map(px -> Tensors.of(px, py)) //
            .map(function))));
  }

  @Override // from GeodesicArrayPlot
  public Tensor pixel2model(Dimension dimension) {
    return HsArrayPlot.pixel2model(coordinateBoundingBox, dimension);
  }
}
