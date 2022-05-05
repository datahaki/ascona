// code by jph
package ch.alpine.ascona.ref.d1h;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.io.GokartPoseDataV2;
import ch.alpine.ascona.util.AbstractDemoHelper;

class HermiteDatasetDemoTest {
  @Test
  public void testSimple() {
    AbstractDemoHelper.offscreen(new HermiteDatasetDemo(GokartPoseDataV2.RACING_DAY));
  }
}
