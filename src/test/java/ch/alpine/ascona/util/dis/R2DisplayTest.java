// code by jph
package ch.alpine.ascona.util.dis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ch.alpine.sophus.lie.rn.RnGroup;

class R2DisplayTest {
  @Test
  void testSimple() {
    assertEquals(R2Display.INSTANCE.geodesicSpace(), RnGroup.INSTANCE);
  }
}
