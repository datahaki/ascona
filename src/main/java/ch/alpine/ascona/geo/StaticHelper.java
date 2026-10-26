// code by jph
package ch.alpine.ascona.geo;

import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Partition;
import ch.alpine.tensor.io.Import;
import ch.alpine.tensor.qty.Degree;
import ch.alpine.tensor.qty.Quantity;

enum StaticHelper {
  ;
  public static Tensor segments() {
    Tensor tensor = Import.of("ch/alpine/ascona/geo/2024_routes.csv");
    return Tensor.of(tensor.stream() //
        .filter(r -> r.length() == 8) //
        .map(r -> Partition.of(r.extract(4, 8).maps(s -> Quantity.of(s, Degree.UNIT)), 2)));
  }
}
