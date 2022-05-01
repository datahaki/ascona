// code by jph
package ch.alpine.ascona.decim;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.AbstractDemoHelper;

class S2DeltaDemoTest {
  @Test
  public void testSimpleV1() {
    AbstractDemoHelper.offscreen(new S2DeltaDemo());
  }
}
