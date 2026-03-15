// code by jph
package ch.alpine.ascona.dat.gok;

import ch.alpine.ascony.win.ControlPosVelSe2;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.qty.Quantity;

public class PosVelHz extends PosHz {
  static final Scalar UNIT = Quantity.of(1.0, "s^-1");
  static final TensorUnaryOperator EXTRACT_VEL = row -> row.extract(5, 8);

  public PosVelHz(Tensor tensor) {
    super(tensor);
  }

  /** @return n x 2 x 3 array */
  public ControlPosVelSe2 getPosVelSequence() {
    TensorUnaryOperator extract = row -> Tensors.of( //
        EXTRACT_POS.apply(row), //
        EXTRACT_VEL.apply(row).multiply(UNIT));
    return new ControlPosVelSe2(extract.slash(tensor));
  }
}
