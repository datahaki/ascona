// code by jph
package ch.alpine.ascona.geo;

import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Partition;
import ch.alpine.tensor.qty.Degree;
import ch.alpine.tensor.qty.Quantity;

record RouteRecord(Scalar localDate, Scalar dist, Scalar up, Scalar dn, Tensor path) {
  static RouteRecord of(Tensor row) {
    Tensor path = Partition.of(row.extract(4, row.length()).maps(s -> Quantity.of(s, Degree.UNIT)), 2);
    // date, km, up, dn, path coordinates
    return new RouteRecord( //
        row.Get(0), //
        row.Get(1), //
        row.Get(2), //
        row.Get(3), //
        path);
  }
}
