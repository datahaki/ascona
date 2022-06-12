// code by jph
package ch.alpine.ascona.util.arp;

import java.util.function.Function;

import ch.alpine.tensor.Tensor;

public record ArrayFunction<T extends Tensor> (Function<Tensor, T> function, T fallback) {
  // ---
}
