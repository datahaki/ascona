// code by jph
package ch.alpine.ascona.decim;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.AbstractDemoHelper;

class BulkDecimationDemoTest {
  @Test
  public void testSimple() {
    AbstractDemoHelper.offscreen(new BulkDecimationDemo());
  }
}
