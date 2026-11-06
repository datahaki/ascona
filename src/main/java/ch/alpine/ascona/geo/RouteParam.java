// code by jph
package ch.alpine.ascona.geo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Flatten;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.qty.DateTime;
import ch.alpine.tensor.qty.Degree;
import ch.alpine.tensor.qty.QuantityMagnitude;
import ch.alpine.tensor.sca.Round;

@ReflectionMarker
class RouteParam {
  static final ScalarUnaryOperator suo = QuantityMagnitude.SI().in(Degree.UNIT);
  public LocalDate localDate = LocalDate.now();
  public Scalar km = RealScalar.ZERO;
  public Integer up = 0;
  public Integer dn = 0;

  public RouteRecord rec(Tensor path) {
    return new RouteRecord(DateTime.of(localDate, LocalTime.MIDNIGHT), km, RealScalar.of(up), RealScalar.of(dn), path);
  }

  public String line(Tensor path) {
    return Stream.concat( //
        Stream.of(DateTime.of(localDate, LocalTime.MIDNIGHT), km, up, dn), //
        Flatten.scalars(path).map(suo).map(Round._7)) //
        .map(Object::toString) //
        .collect(Collectors.joining(","));
  }
}
