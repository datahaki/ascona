// code by jph
package ch.alpine.ascona.bd2;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.AbstractDemoHelper;

class PlanarScatteredSetCoordinateDemoTest {
  @Test
  public void testSimple() {
    AbstractDemoHelper.offscreen(new PlanarScatteredSetCoordinateDemo());
  }
}
