// code by jph
package ch.alpine.ascona.crv.se2c;

import ch.alpine.sophus.crv.se2c.EulerSpiral;
import ch.alpine.sophus.crv.se2c.LogarithmicSpiral;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;

public enum SpiralParam {
  EULER(EulerSpiral.FUNCTION),
  LOGARITHMIC(LogarithmicSpiral.of(1, 0.2));

  public final ScalarTensorFunction scalarTensorFunction;
  public final Tensor points;

  SpiralParam(ScalarTensorFunction scalarTensorFunction) {
    this.scalarTensorFunction = scalarTensorFunction;
    points = Subdivide.of(-10.0, 10.0, 10000).map(scalarTensorFunction).unmodifiable();
  }
}
