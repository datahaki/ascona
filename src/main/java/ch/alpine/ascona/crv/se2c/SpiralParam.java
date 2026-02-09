// code by jph
package ch.alpine.ascona.crv.se2c;

import ch.alpine.sophus.lie.se2.EulerSpiral;
import ch.alpine.sophus.lie.se2.LogarithmicSpiral;
import ch.alpine.tensor.api.ScalarTensorFunction;

public enum SpiralParam {
  EULER(EulerSpiral.FUNCTION),
  LOGARITHMIC1(LogarithmicSpiral.of(1, 0.2)),
  LOGARITHMIC2(LogarithmicSpiral.of(0.5, 0.3)),
  //
  ;

  public final ScalarTensorFunction scalarTensorFunction;

  SpiralParam(ScalarTensorFunction scalarTensorFunction) {
    this.scalarTensorFunction = scalarTensorFunction;
  }
}
