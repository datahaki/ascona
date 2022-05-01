// code by jph
package ch.alpine.ascona.bdn;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.AbstractDemoHelper;

class Se2ScatteredSetCoordinateDemoTest {
  @Test
  public void testSimple() {
    AbstractDemoHelper.offscreen(new Se2ScatteredSetCoordinateDemo());
  }
}
