// code by jph
package ch.alpine.ascona.misc;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.AbstractDemoHelper;

class S2LineDistanceDemoTest {
  @Test
  public void testSimple() {
    AbstractDemoHelper.offscreen(new S2LineDistanceDemo());
  }
}
