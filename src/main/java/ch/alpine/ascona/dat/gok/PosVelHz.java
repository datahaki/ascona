// code by jph
package ch.alpine.ascona.dat.gok;

import ch.alpine.ascony.win.ControlPosVelSe2;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.qty.Quantity;

public class PosVelHz extends PosHz {
  static final TensorUnaryOperator EXTRACT_VEL = row -> row.extract(5, 8).maps(s -> Quantity.of(s, "s^-1"));
  static final TensorUnaryOperator EXTRACT = row -> Tensors.of( //
      EXTRACT_POS.apply(row), //
      EXTRACT_VEL.apply(row));

  public PosVelHz(Tensor tensor) {
    super(tensor);
  }

  public ControlPosVelSe2 getPosVelSequence() {
    return new ControlPosVelSe2(EXTRACT.slash(tensor));
  }
}
