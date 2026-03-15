// code by jph
package ch.alpine.ascona.dat.gok;

import java.io.Serializable;

import ch.alpine.ascony.win.ControlPosSe2;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Differences;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.qty.Quantity;
import ch.alpine.tensor.red.Mean;

public class PosHz implements Serializable {
  static final TensorUnaryOperator EXTRACT_POS = row -> row.extract(1, 4);
  // private static final TensorUnaryOperator EXTRACT_WITH_UNITS = row -> Tensors.of( //
  // Quantity.of(row.Get(1), "m"), //
  // Quantity.of(row.Get(2), "m"), //
  // row.Get(3));
  protected final Tensor tensor;

  public PosHz(Tensor tensor) {
    this.tensor = tensor;
  }

  public final ControlPosSe2 getPosSequence() {
    return new ControlPosSe2(EXTRACT_POS.slash(tensor));
  }

  public final Scalar getSamplingRate() {
    return Quantity.of(Mean.ofVector(Differences.of(tensor.get(Tensor.ALL, 0))).reciprocal(), "Hz");
  }
}
