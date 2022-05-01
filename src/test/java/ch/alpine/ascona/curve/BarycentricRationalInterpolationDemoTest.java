// code by jph
package ch.alpine.ascona.curve;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.AbstractDemoHelper;

class BarycentricRationalInterpolationDemoTest {
  @Test
  public void testSimple() {
    AbstractDemoHelper.offscreen(new BarycentricRationalInterpolationDemo());
  }
}
