// code by jph
package ch.alpine.ascona.decim;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.util.AbstractDemoHelper;
import ch.alpine.ascona.util.dat.GokartPoseDataV1;
import ch.alpine.ascona.util.dat.GokartPoseDataV2;

class CurveDecimationDemoTest {
  @Test
  public void testSimpleV1() {
    AbstractDemoHelper.offscreen(new CurveDecimationDemo(GokartPoseDataV1.INSTANCE));
  }

  @Test
  public void testSimpleV2() {
    AbstractDemoHelper.offscreen(new CurveDecimationDemo(GokartPoseDataV2.INSTANCE));
  }
}
