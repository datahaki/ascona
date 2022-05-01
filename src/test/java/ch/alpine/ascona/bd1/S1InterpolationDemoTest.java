// code by jph
package ch.alpine.ascona.bd1;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.AbstractDemoHelper;

class S1InterpolationDemoTest {
  @Test
  public void testSimple() {
    AbstractDemoHelper.offscreen(new S1InterpolationDemo());
  }
}
