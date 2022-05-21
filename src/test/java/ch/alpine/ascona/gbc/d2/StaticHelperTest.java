// code by jph
package ch.alpine.ascona.gbc.d2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.util.arp.HsArrayPlots;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.sca.Clips;

class StaticHelperTest {
  @Test
  public void testSimple() {
    assertEquals(HsArrayPlots.cover(Clips.interval(2, 4), RealScalar.of(1)), Clips.interval(1, 4));
    assertEquals(HsArrayPlots.cover(Clips.interval(2, 4), RealScalar.of(3)), Clips.interval(2, 4));
    assertEquals(HsArrayPlots.cover(Clips.interval(2, 4), RealScalar.of(5)), Clips.interval(2, 5));
  }
}
