// code by jph
package ch.alpine.ascona.misc;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.AbstractDemoHelper;

class ColoredNoiseDemoTest {
  @Test
  public void testSimple() {
    AbstractDemoHelper.offscreen(new ColoredNoiseDemo());
  }
}
