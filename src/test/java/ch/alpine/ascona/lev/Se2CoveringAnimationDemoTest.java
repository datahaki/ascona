// code by jph
package ch.alpine.ascona.lev;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.AbstractDemoHelper;

class Se2CoveringAnimationDemoTest {
  @Test
  public void testSimple() {
    AbstractDemoHelper.offscreen(new Se2CoveringAnimationDemo());
  }
}
