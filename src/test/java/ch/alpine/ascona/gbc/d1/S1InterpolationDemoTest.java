// code by jph
package ch.alpine.ascona.gbc.d1;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.util.win.AbstractDemoHelper;

class S1InterpolationDemoTest {
  @Test
  void test() {
    AbstractDemoHelper abstractDemoHelper = AbstractDemoHelper.offscreen(new S1InterpolationDemo());
    assertEquals(abstractDemoHelper.errors(), 0);
  }
}
