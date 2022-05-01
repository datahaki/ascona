// code by jph
package ch.alpine.ascona.curve;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.AbstractDemoHelper;

class R2BSplineFunctionDemoTest {
  @Test
  public void testSimple() {
    AbstractDemoHelper.offscreen(new R2BSplineFunctionDemo());
  }
}
