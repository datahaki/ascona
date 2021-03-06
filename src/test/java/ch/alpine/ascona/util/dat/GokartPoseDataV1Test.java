// code by jph
package ch.alpine.ascona.util.dat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import ch.alpine.tensor.qty.Quantity;

class GokartPoseDataV1Test {
  @Test
  void testSampleRate() {
    assertEquals(GokartPoseDataV1.INSTANCE.getSampleRate(), Quantity.of(20, "s^-1"));
  }

  @Test
  void testListUnmodifiable() {
    try {
      GokartPoseDataV1.INSTANCE.list().clear();
      fail();
    } catch (Exception exception) {
      // ---
    }
  }
}
