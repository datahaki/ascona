// code by jph
package ch.alpine.ascona.filter;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.AbstractDemoHelper;

class Se2BiinvariantMeanDemoTest {
  @Test
  public void testSimple() {
    AbstractDemoHelper.offscreen(new Se2BiinvariantMeanDemo());
  }
}
