// code by jph
package ch.alpine.ascona.geo;

import java.util.List;
import java.util.stream.Collectors;

import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Throw;
import ch.alpine.tensor.io.Import;
import ch.alpine.tensor.io.StringScalarQ;

enum StaticHelper {
  ;
  public static List<RouteRecord> segments() {
    Tensor tensor = Import.of("ch/alpine/ascona/geo/2024_routes.csv");
    Throw.unless(!StringScalarQ.any(tensor));
    return tensor.stream().map(RouteRecord::of).collect(Collectors.toList());
  }
}
