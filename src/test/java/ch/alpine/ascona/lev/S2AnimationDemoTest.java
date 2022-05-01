// code by jph
package ch.alpine.ascona.lev;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.AbstractDemoHelper;

class S2AnimationDemoTest {
  @Test
  public void testSimple() {
    AbstractDemoHelper.offscreen(new S2AnimationDemo());
  }
}
