// code by jph
package ch.alpine.ascona.misc;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.AbstractDemoHelper;

class S2LogDemoTest {
  @Test
  public void testSimple() {
    AbstractDemoHelper.offscreen(new S2LogDemo());
  }
}
