// code by jph
package ch.alpine.ascona.util.arp;

import java.util.Optional;
import java.util.function.Function;

import ch.alpine.tensor.Tensor;

public record ArrayFunction<T extends Tensor>(Function<Tensor, T> function, T fallback) //
    implements Function<Optional<Tensor>, T> {
  /** @param optional
   * @return */
  @Override
  public T apply(Optional<Tensor> optional) {
    return optional.map(function).orElse(fallback);
  }
}
