// code by jph
package ch.alpine.ascona.bd1;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.AbstractDemoHelper;

class S2AveragingDemoTest {
  @Test
  public void testSimple() {
    AbstractDemoHelper.offscreen(new S2AveragingDemo());
  }
}
