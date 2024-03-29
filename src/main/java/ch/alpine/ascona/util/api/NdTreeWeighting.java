// code by jph
package ch.alpine.ascona.util.api;

import java.io.Serializable;
import java.util.Collection;

import ch.alpine.sophus.dv.Biinvariant;
import ch.alpine.sophus.hs.Sedarim;
import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorScalarFunction;
import ch.alpine.tensor.io.MathematicaFormat;
import ch.alpine.tensor.nrm.NormalizeTotal;
import ch.alpine.tensor.opt.nd.CoordinateBounds;
import ch.alpine.tensor.opt.nd.NdCenters;
import ch.alpine.tensor.opt.nd.NdCollectNearest;
import ch.alpine.tensor.opt.nd.NdMap;
import ch.alpine.tensor.opt.nd.NdMatch;
import ch.alpine.tensor.opt.nd.NdTreeMap;

/** concept is mathematically not sound
 * in the best case, the issues are restricted to inconsistent distance/metric,
 * and non-differentiability */
public class NdTreeWeighting implements LogWeighting, Serializable {
  private final int limit;

  public NdTreeWeighting(int limit) {
    this.limit = limit;
  }

  @Override
  public Sedarim sedarim(Biinvariant biinvariant, ScalarUnaryOperator variogram, Tensor sequence) {
    throw new UnsupportedOperationException();
  }

  @Override
  public TensorScalarFunction function(Biinvariant biinvariant, //
      ScalarUnaryOperator variogram, Tensor sequence, Tensor values) {
    NdMap<Scalar> ndMap = NdTreeMap.of(CoordinateBounds.of(sequence));
    for (int index = 0; index < values.length(); ++index)
      ndMap.insert(sequence.get(index), values.Get(index));
    return new Inner(ndMap, variogram);
  }

  @Override // from Object
  public String toString() {
    return MathematicaFormat.concise("NdTreeWeighting", limit);
  }

  private class Inner implements TensorScalarFunction {
    private final NdMap<Scalar> ndMap;
    private final ScalarUnaryOperator variogram;

    public Inner(NdMap<Scalar> ndMap, ScalarUnaryOperator variogram) {
      this.ndMap = ndMap;
      this.variogram = variogram;
    }

    @Override
    public Scalar apply(Tensor center) {
      Collection<NdMatch<Scalar>> collection = NdCollectNearest.of(ndMap, NdCenters.VECTOR_2_NORM.apply(center), limit);
      if (collection.isEmpty())
        return DoubleScalar.INDETERMINATE;
      Tensor weights = NormalizeTotal.FUNCTION.apply( //
          Tensor.of(collection.stream().map(NdMatch::distance).map(variogram)));
      return (Scalar) weights.dot(Tensor.of(collection.stream().map(NdMatch::value)));
    }
  }
}
