// code by jph
package ch.alpine.ascona.util.arp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.red.Max;
import ch.alpine.tensor.red.Min;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;

class ArrayPlotRenderTest {
  /** @param clip
   * @param scalar
   * @return */
  /* package */ static Clip cover(Clip clip, Scalar scalar) {
    return Clips.interval( //
        Min.of(clip.min(), scalar), //
        Max.of(clip.max(), scalar));
  }

  @Test
  void testSimple() {
    assertEquals(cover(Clips.interval(2, 4), RealScalar.of(1)), Clips.interval(1, 4));
    assertEquals(cover(Clips.interval(2, 4), RealScalar.of(3)), Clips.interval(2, 4));
    assertEquals(cover(Clips.interval(2, 4), RealScalar.of(5)), Clips.interval(2, 5));
  }
}
