// code by jph
package ch.alpine.ascona.util.api;

import java.util.Objects;

import ch.alpine.sophus.gbc.d2.InsideConvexHullCoordinate;
import ch.alpine.sophus.hs.Biinvariant;
import ch.alpine.sophus.hs.Genesis;
import ch.alpine.sophus.hs.HsGenesis;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorScalarFunction;
import ch.alpine.tensor.api.TensorUnaryOperator;

public class InsideConvexHullLogWeighting implements LogWeighting {
  private final Genesis genesis;

  public InsideConvexHullLogWeighting(Genesis genesis) {
    this.genesis = Objects.requireNonNull(genesis);
  }

  @Override // from LogWeighting
  public TensorUnaryOperator operator( //
      Biinvariant biinvariant, // <- ignored
      ScalarUnaryOperator variogram, // <- ignored
      Tensor sequence) {
    return HsGenesis.wrap( //
        biinvariant.hsDesign(), //
        new InsideConvexHullCoordinate(genesis), //
        sequence);
  }

  @Override // from LogWeighting
  public TensorScalarFunction function( //
      Biinvariant biinvariant, // <- ignored
      ScalarUnaryOperator variogram, // <- ignored
      Tensor sequence, Tensor values) {
    TensorUnaryOperator tensorUnaryOperator = operator(biinvariant, variogram, sequence);
    Objects.requireNonNull(values);
    return point -> (Scalar) tensorUnaryOperator.apply(point).dot(values);
  }
}
