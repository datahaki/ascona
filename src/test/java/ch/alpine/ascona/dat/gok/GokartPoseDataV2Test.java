// code by jph
package ch.alpine.ascona.dat.gok;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GokartPoseDataV2Test {
  @Test
  void testSampleRate() {
    // assertEquals(GokartPosVel.INSTANCE.getSampleRate(), Quantity.of(50, "s^-1"));
  }

  @Test
  void testRacingLength() {
    assertTrue(18 <= GokartPos.list().size());
    assertTrue(18 <= GokartPosVel.list().size());
  }

  @Test
  void testListUnmodifiable() {
    assertThrows(Exception.class, () -> GokartPos.list().clear());
    assertThrows(Exception.class, () -> GokartPosVel.list().clear());
  }
}
