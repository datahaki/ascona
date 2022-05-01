// code by jph
package ch.alpine.ascona.bdn;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.AbstractDemoHelper;

class R2DeformationDemoTest {
  @Test
  public void testSimple() {
    AbstractDemoHelper.offscreen(new R2DeformationDemo());
  }
}
