// code by jph
package ch.alpine.ascona.util.arp;

import java.util.function.Function;

import ch.alpine.bridge.fig.ArrayPlot;
import ch.alpine.tensor.Tensor;

/** @see ArrayPlot */
@FunctionalInterface
public interface HsArrayPlot {
  /** @param resolution
   * @param function
   * @param fallback
   * @return */
  Tensor raster(int resolution, Function<Tensor, ? extends Tensor> function, Tensor fallback);
}
