// code by jph
package ch.alpine.ascona.util.ren;

import org.junit.jupiter.api.Test;

import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.mat.Tolerance;

class HueColorDataTest {
  @Test
  void test() {
    double goldenAngle = (3 - Math.sqrt(5)) * 0.5;
    Tolerance.CHOP.requireClose(HueColorData.GOLDEN_ANGLE, RealScalar.of(goldenAngle));
  }
}
