// code by jph
package ch.alpine.ascona.util.api;

import java.util.ArrayList;
import java.util.List;

import ch.alpine.sophus.dv.Biinvariant;
import ch.alpine.sophus.hs.Sedarim;
import ch.alpine.sophus.itp.RadialBasisFunctionInterpolation;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorScalarFunction;
import ch.alpine.tensor.api.TensorUnaryOperator;

public enum MixedLogWeightings implements LogWeighting {
  RADIAL_BASIS {
    @Override
    public Sedarim sedarim(Biinvariant biinvariant, ScalarUnaryOperator variogram, Tensor sequence) {
      return RadialBasisFunctionInterpolation.of( //
          biinvariant.var_dist(variogram, sequence), //
          sequence)::apply;
    }

    @Override
    public TensorScalarFunction function( //
        Biinvariant biinvariant, ScalarUnaryOperator variogram, Tensor sequence, Tensor values) {
      TensorUnaryOperator tensorUnaryOperator = RadialBasisFunctionInterpolation.of( //
          biinvariant.var_dist(variogram, sequence), //
          sequence, values);
      return tensor -> (Scalar) tensorUnaryOperator.apply(tensor);
    }
  };

  public static List<LogWeighting> scattered() { //
    List<LogWeighting> list = new ArrayList<>();
    list.addAll(LogWeightings.list());
    list.addAll(List.of(values()));
    return list;
  }
}
