// code by jph
package ch.alpine.ascona.bd1;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.AbstractDemoHelper;

class R1KrigingDemoTest {
  @Test
  public void testSimple() {
    AbstractDemoHelper.offscreen(new R1KrigingDemo());
  }
}
