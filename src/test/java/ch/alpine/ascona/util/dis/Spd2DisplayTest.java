// code by jph
package ch.alpine.ascona.util.dis;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.PadRight;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.mat.PositiveDefiniteMatrixQ;
import ch.alpine.tensor.sca.Chop;

class Spd2DisplayTest {
  private static final ManifoldDisplay GEODESIC_DISPLAY = Spd2Display.INSTANCE;

  @Test
  void testSimple() {
    Tensor tensor = GEODESIC_DISPLAY.xya2point(Tensors.vector(1, 0.2, -1));
    assertTrue(PositiveDefiniteMatrixQ.ofHermitian(tensor));
    Tensor vector = GEODESIC_DISPLAY.point2xy(tensor);
    Chop._10.requireClose(vector, Tensors.vector(1, 0.2));
    Tensor lift = GEODESIC_DISPLAY.matrixLift(tensor);
    TensorUnaryOperator tensorUnaryOperator = PadRight.zeros(2, 2);
    Chop._10.requireClose(tensorUnaryOperator.apply(lift), tensor);
  }
}
