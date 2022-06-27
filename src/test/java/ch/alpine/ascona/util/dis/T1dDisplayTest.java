// code by jph
package ch.alpine.ascona.util.dis;

import org.junit.jupiter.api.Test;

import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.mat.Tolerance;

class T1dDisplayTest {
  @Test
  void test() {
    Tensor xya = Tensors.vector(-1, -2, -3);
    Tensor q = T1dDisplay.INSTANCE.xya2point(xya);
    Tensor xy0 = T1dDisplay.INSTANCE.point2xya(q);
    Tolerance.CHOP.requireClose(xya.extract(0, 2), xy0.extract(0, 2));
  }
}
