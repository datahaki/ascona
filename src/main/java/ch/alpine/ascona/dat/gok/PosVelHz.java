// code by jph
package ch.alpine.ascona.dat.gok;

import java.io.Serializable;

import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Differences;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.qty.Quantity;
import ch.alpine.tensor.red.Mean;

public class PosVelHz implements Serializable {
  private static final TensorUnaryOperator EXTRACT = row -> Tensors.of(row.extract(1, 4), row.extract(5, 8));
  // ---
  private final Tensor tensor;

  public PosVelHz(Tensor tensor) {
    this.tensor = tensor;
  }

  public Tensor getPosVelSequence() {
    return EXTRACT.slash(tensor);
  }

  public Scalar getSamplingRate() {
    return Quantity.of(Mean.ofVector(Differences.of(tensor.get(Tensor.ALL, 0))).reciprocal(), "Hz");
  }
}
