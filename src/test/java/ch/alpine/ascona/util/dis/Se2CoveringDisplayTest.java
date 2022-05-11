// code by jph
package ch.alpine.ascona.util.dis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ch.alpine.sophus.lie.se2c.Se2CoveringGroup;

class Se2CoveringDisplayTest {
  @Test
  public void testSimple() {
    assertEquals(Se2CoveringDisplay.INSTANCE.geodesicSpace(), Se2CoveringGroup.INSTANCE);
  }
}
