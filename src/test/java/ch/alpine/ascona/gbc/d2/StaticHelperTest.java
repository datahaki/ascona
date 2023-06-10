// code by jph
package ch.alpine.ascona.gbc.d2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.gbc.d2.StaticHelper.IntBlend;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.mat.Tolerance;

class StaticHelperTest {
  @Test
  void testIntBlend() {
    IntBlend intBlend = new IntBlend(RealScalar.of(0.1));
    assertEquals(intBlend.apply(RealScalar.of(2.5)), RealScalar.ONE);
    Tolerance.CHOP.requireClose(intBlend.apply(RealScalar.of(3.95)), RationalScalar.HALF);
  }
}
