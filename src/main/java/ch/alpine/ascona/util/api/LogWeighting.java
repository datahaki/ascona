// code by jph
package ch.alpine.ascona.util.api;

import ch.alpine.sophus.hs.Biinvariant;
import ch.alpine.sophus.hs.Manifold;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorScalarFunction;
import ch.alpine.tensor.api.TensorUnaryOperator;

public interface LogWeighting {
  /** @param biinvariant
   * @param manifold
   * @param variogram
   * @param sequence
   * @return */
  TensorUnaryOperator operator( //
      Biinvariant biinvariant, Manifold manifold, ScalarUnaryOperator variogram, Tensor sequence);

  /** @param biinvariant
   * @param manifold
   * @param variogram
   * @param sequence
   * @param values
   * @return */
  TensorScalarFunction function( //
      Biinvariant biinvariant, Manifold manifold, ScalarUnaryOperator variogram, //
      Tensor sequence, Tensor values);
}
