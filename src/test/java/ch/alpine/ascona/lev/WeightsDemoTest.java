// code by jph
package ch.alpine.ascona.lev;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.AbstractDemoHelper;

class WeightsDemoTest {
  @Test
  public void testSimple() {
    AbstractDemoHelper.offscreen(new WeightsDemo());
  }
}
