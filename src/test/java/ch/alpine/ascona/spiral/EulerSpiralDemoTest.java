// code by jph
package ch.alpine.ascona.spiral;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.AbstractDemoHelper;

class EulerSpiralDemoTest {
  @Test
  public void testSimple() {
    AbstractDemoHelper.offscreen(new EulerSpiralDemo());
  }
}
