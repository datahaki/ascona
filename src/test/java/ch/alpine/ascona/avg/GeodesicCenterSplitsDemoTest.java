// code by jph
package ch.alpine.ascona.avg;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.AbstractDemoHelper;

class GeodesicCenterSplitsDemoTest {
  @Test
  public void testSimple() {
    AbstractDemoHelper.offscreen(new GeodesicCenterSplitsDemo());
  }
}
