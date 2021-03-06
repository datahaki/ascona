// code by jph
package ch.alpine.ascona.util.api;

import java.util.List;

import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.ArrayReshape;
import ch.alpine.tensor.alg.Dimensions;
import ch.alpine.tensor.alg.Transpose;

public enum ImageTiling {
  ;
  /** @param wgs slices of images as tensor of rank at least 3
   * @return tensor with rank of wgs - 1 */
  public static Tensor of(Tensor wgs) {
    List<Integer> list = Dimensions.of(wgs);
    return ArrayReshape.of(Transpose.of(wgs, 0, 2, 1), list.get(0), list.get(1) * list.get(2));
  }
}
