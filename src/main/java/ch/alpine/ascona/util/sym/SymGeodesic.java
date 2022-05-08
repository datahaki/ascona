// code by jph
package ch.alpine.ascona.util.sym;

import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.ScalarTensorFunction;

public enum SymGeodesic implements GeodesicSpace {
  INSTANCE;

  @Override // from Geodesic
  public ScalarTensorFunction curve(Tensor p, Tensor q) {
    return scalar -> split(p, q, scalar);
  }

  @Override // from GeodesicInterface
  public Tensor split(Tensor p, Tensor q, Scalar scalar) {
    return new SymScalarPart((Scalar) p, (Scalar) q, scalar);
  }
}
