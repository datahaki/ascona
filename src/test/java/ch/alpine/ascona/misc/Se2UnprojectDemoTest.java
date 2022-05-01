// code by jph
package ch.alpine.ascona.misc;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.AbstractDemoHelper;

class Se2UnprojectDemoTest {
  @Test
  public void testSimple() {
    AbstractDemoHelper.offscreen(new Se2UnprojectDemo());
  }
}
