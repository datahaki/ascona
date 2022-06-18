// code by jph
package ch.alpine.ascona.lev;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ch.alpine.ascona.util.win.AbstractDemoHelper;

class WeightsDemoTest {
  @Test
  @Disabled
  void test() {
    WeightsDemo weightsDemo = new WeightsDemo();
    AbstractDemoHelper.offscreen(weightsDemo);
  }
}
