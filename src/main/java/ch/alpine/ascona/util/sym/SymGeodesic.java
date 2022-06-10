// code by jph
package ch.alpine.ascona.util.sym;

import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.ScalarTensorFunction;

public enum SymGeodesic implements GeodesicSpace {
  INSTANCE;

  @Override
  public ScalarTensorFunction curve(Tensor p, Tensor q) {
    return scalar -> new SymScalarPart((Scalar) p, (Scalar) q, scalar);
  }
}
