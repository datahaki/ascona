// code by jph
package ch.alpine.ascona.util.arp;

import java.awt.Dimension;
import java.util.function.Function;

import ch.alpine.bridge.fig.ArrayPlot;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.sophus.math.AppendOne;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Dot;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.red.Times;

/** @see ArrayPlot */
public interface HsArrayPlot {
  /** @param resolution
   * @param function
   * @param fallback
   * @return */
  Tensor raster(int resolution, Function<Tensor, ? extends Tensor> function, Tensor fallback);

  /** @param dimension
   * @return */
  Tensor pixel2model(Dimension dimension);

  /** @param xy lower left corner
   * @param range of image in model space
   * @param dimension of image
   * @return */
  static Tensor pixel2model(CoordinateBoundingBox coordinateBoundingBox, Dimension dimension) {
    // pixel 2 model
    Tensor xy = Tensors.of(coordinateBoundingBox.getClip(0).min(), coordinateBoundingBox.getClip(1).min());
    Tensor range = Tensors.of(coordinateBoundingBox.getClip(0).width(), coordinateBoundingBox.getClip(1).width());
    Tensor scale = Times.of(range, Tensors.vector(dimension.width, dimension.height).map(Scalar::reciprocal));
    return Dot.of( //
        GfxMatrix.translation(xy), //
        Times.of(AppendOne.FUNCTION.apply(scale), GfxMatrix.flipY(dimension.height)));
  }
}
