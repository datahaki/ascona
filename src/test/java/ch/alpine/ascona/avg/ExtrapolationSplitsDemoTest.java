// code by jph
package ch.alpine.ascona.avg;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.AbstractDemoHelper;

class ExtrapolationSplitsDemoTest {
  @Test
  public void testSimple() {
    AbstractDemoHelper.offscreen(new ExtrapolationSplitsDemo());
  }
}
