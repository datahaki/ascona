// code by jph
package ch.alpine.ascona.util.api;

import ch.alpine.sophus.dv.Biinvariant;
import ch.alpine.sophus.hs.Sedarim;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorScalarFunction;

public interface LogWeighting {
  /** @param biinvariant
   * @param manifold
   * @param variogram
   * @param sequence
   * @return */
  Sedarim operator( //
      Biinvariant biinvariant, ScalarUnaryOperator variogram, Tensor sequence);

  /** @param biinvariant
   * @param manifold
   * @param variogram
   * @param sequence
   * @param values
   * @return */
  TensorScalarFunction function( //
      Biinvariant biinvariant, ScalarUnaryOperator variogram, Tensor sequence, Tensor values);
}
