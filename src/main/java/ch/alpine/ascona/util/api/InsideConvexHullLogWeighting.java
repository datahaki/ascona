// code by jph
package ch.alpine.ascona.util.api;

import java.util.Objects;

import ch.alpine.sophus.dv.Biinvariant;
import ch.alpine.sophus.gbc.d2.InsideConvexHullCoordinate;
import ch.alpine.sophus.hs.Genesis;
import ch.alpine.sophus.hs.HsGenesis;
import ch.alpine.sophus.hs.Sedarim;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorScalarFunction;

public class InsideConvexHullLogWeighting implements LogWeighting {
  private final Genesis genesis;

  public InsideConvexHullLogWeighting(Genesis genesis) {
    this.genesis = Objects.requireNonNull(genesis);
  }

  @Override // from LogWeighting
  public Sedarim operator( //
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
      Biinvariant biinvariant, //
      ScalarUnaryOperator variogram, // <- ignored
      Tensor sequence, Tensor values) {
    Sedarim sedarim = operator(biinvariant, variogram, sequence);
    Objects.requireNonNull(values);
    return point -> (Scalar) sedarim.sunder(point).dot(values);
  }
}
