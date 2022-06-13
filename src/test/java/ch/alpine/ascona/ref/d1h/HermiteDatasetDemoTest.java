// code by jph
package ch.alpine.ascona.ref.d1h;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.util.dat.GokartPoseDataV2;
import ch.alpine.ascona.util.win.AbstractDemoHelper;

class HermiteDatasetDemoTest {
  @Test
  void testSimple() {
    AbstractDemoHelper.offscreen(new HermiteDatasetDemo(GokartPoseDataV2.RACING_DAY));
  }
}
