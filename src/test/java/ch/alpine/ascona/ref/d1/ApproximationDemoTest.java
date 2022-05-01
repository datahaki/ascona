// code by jph
package ch.alpine.ascona.ref.d1;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.AbstractDemoHelper;
import ch.alpine.ascona.io.GokartPoseDataV2;

class ApproximationDemoTest {
  @Test
  public void testSimple() {
    AbstractDemoHelper.offscreen(new ApproximationDemo(GokartPoseDataV2.RACING_DAY));
  }
}
