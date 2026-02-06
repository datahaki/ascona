// code by jph
package ch.alpine.ascona.decim;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.dat.GokartPos;
import ch.alpine.ascony.win.AbstractDemoHelper;

class CurveDecimationDemoTest {
  @Test
  void testSimpleV1() {
    AbstractDemoHelper.offscreen(new CurveDecimationDemo(new GokartPos()));
  }

  @Test
  void testSimpleV2() {
    AbstractDemoHelper.offscreen(new CurveDecimationDemo(new GokartPos()));
  }
}
