package ch.alpine.ascona.dat.gok;

import java.io.Serializable;

import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Differences;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.qty.Quantity;
import ch.alpine.tensor.red.Mean;

public class PosHz implements Serializable {
  private static final TensorUnaryOperator EXTRACT = row -> row.extract(1, 4);
  private final Tensor tensor;

  public PosHz(Tensor tensor) {
    this.tensor = tensor;
  }

  public Tensor getPoseSequence() {
    return EXTRACT.slash(tensor);
  }

  public Scalar getSamplingRate() {
    return Quantity.of(Mean.ofVector(Differences.of(tensor.get(Tensor.ALL, 0))).reciprocal(), "Hz");
  }
}
