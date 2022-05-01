// code by jph
package ch.alpine.ascona.curve;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.AbstractDemoHelper;

class DeBoorCustomDemoTest {
  @Test
  public void testSimple() {
    AbstractDemoHelper.offscreen(new DeBoorCustomDemo());
  }
}
