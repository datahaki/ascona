// code by jph
package ch.alpine.ascona.sym;

import ch.alpine.sophus.api.Geodesic;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.ScalarTensorFunction;

public enum SymGeodesic implements Geodesic {
  INSTANCE;

  @Override // from Geodesic
  public ScalarTensorFunction curve(Tensor p, Tensor q) {
    return scalar -> split(p, q, scalar);
  }

  @Override // from GeodesicInterface
  public Tensor split(Tensor p, Tensor q, Scalar scalar) {
    return SymScalar.of((Scalar) p, (Scalar) q, scalar);
  }
}
