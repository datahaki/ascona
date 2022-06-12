// code by jph
package ch.alpine.ascona.util.api;

import java.util.ArrayList;
import java.util.List;

import ch.alpine.sophus.hs.Biinvariant;
import ch.alpine.sophus.hs.Manifold;
import ch.alpine.sophus.itp.RadialBasisFunctionInterpolation;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorScalarFunction;
import ch.alpine.tensor.api.TensorUnaryOperator;

public enum MixedLogWeightings implements LogWeighting {
  RADIAL_BASIS {
    @Override
    public TensorUnaryOperator operator( //
        Biinvariant biinvariant, Manifold manifold, //
        ScalarUnaryOperator variogram, Tensor sequence) {
      return RadialBasisFunctionInterpolation.of( //
          biinvariant.var_dist(manifold, variogram, sequence), //
          sequence);
    }

    @Override
    public TensorScalarFunction function( //
        Biinvariant biinvariant, Manifold manifold, //
        ScalarUnaryOperator variogram, Tensor sequence, Tensor values) {
      TensorUnaryOperator tensorUnaryOperator = RadialBasisFunctionInterpolation.of( //
          biinvariant.var_dist(manifold, variogram, sequence), //
          sequence, values);
      return tensor -> (Scalar) tensorUnaryOperator.apply(tensor);
    }
  },;

  public static List<LogWeighting> scattered() { //
    List<LogWeighting> list = new ArrayList<>();
    list.addAll(LogWeightings.list());
    list.addAll(List.of(values()));
    return list;
  }
}
