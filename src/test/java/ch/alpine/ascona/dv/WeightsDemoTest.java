// code by jph
package ch.alpine.ascona.dv;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.util.win.AbstractDemoHelper;

class WeightsDemoTest {
  @Test
  void test() {
    AbstractDemoHelper abstractDemoHelper = AbstractDemoHelper.offscreen(new WeightsDemo());
    assertEquals(abstractDemoHelper.errors(), 0);
  }
}