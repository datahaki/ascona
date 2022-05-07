// code by jph
package ch.alpine.ascona.util.sym;

import java.util.stream.IntStream;

import ch.alpine.tensor.Tensor;

public enum SymSequence {
  ;
  /** @param length
   * @return vector of given length and node entries indexed 0, 1, 2, ..., length - 1 */
  public static Tensor of(int length) {
    return Tensor.of(IntStream.range(0, length).mapToObj(SymScalar::leaf));
  }
}
