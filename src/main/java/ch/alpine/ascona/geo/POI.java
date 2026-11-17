// code by jph
package ch.alpine.ascona.geo;

import ch.alpine.bridge.math.Unitless;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;

record POI(Scalar lat, Scalar lon) {
  private static final Tensor DOT = Tensors.fromString("{1,1/60,1/3600}");

  public static POI of(String lat, String lon) {
    Scalar _1 = Scalars.fromString(lat.substring(0, 2));
    Scalar _2 = Scalars.fromString(lat.substring(3, 5));
    Scalar _3 = Scalars.fromString(lat.substring(6, 10));
    Scalar _4 = Scalars.fromString(lon.substring(0, 3));
    Scalar _5 = Scalars.fromString(lon.substring(4, 6));
    Scalar _6 = Scalars.fromString(lon.substring(7, 11));
    Scalar sign = switch (lon.charAt(lon.length() - 1)) {
    case 'E' -> RealScalar.ONE;
    case 'W' -> RealScalar.ONE.negate();
    default -> throw new IllegalArgumentException("Unexpected value: " + lon);
    };
    return new POI( //
        Unitless.DEGREES.of((Scalar) DOT.dot(Tensors.of(_1, _2, _3))), //
        Unitless.DEGREES.of((Scalar) DOT.dot(Tensors.of(_4, _5, _6))).multiply(sign));
  }

  Tensor vector() {
    return Tensors.of(lat, lon);
  }
}
