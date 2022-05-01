// code by jph
package ch.alpine.ascona.bdn;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.AbstractDemoHelper;

class H2DeformationDemoTest {
  @Test
  public void testSimple() {
    AbstractDemoHelper.offscreen(new H2DeformationDemo());
  }
}
