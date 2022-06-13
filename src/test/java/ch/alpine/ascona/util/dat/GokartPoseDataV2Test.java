// code by jph
package ch.alpine.ascona.util.dat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ch.alpine.tensor.qty.Quantity;

class GokartPoseDataV2Test {
  @Test
  void testSampleRate() {
    assertEquals(GokartPoseDataV2.INSTANCE.getSampleRate(), Quantity.of(50, "s^-1"));
  }

  @Test
  void testRacingLength() {
    assertTrue(18 <= GokartPoseDataV2.RACING_DAY.list().size());
  }

  @Test
  void testListUnmodifiable() {
    assertThrows(Exception.class, () -> GokartPoseDataV2.INSTANCE.list().clear());
    assertThrows(Exception.class, () -> GokartPoseDataV2.RACING_DAY.list().clear());
  }
}
