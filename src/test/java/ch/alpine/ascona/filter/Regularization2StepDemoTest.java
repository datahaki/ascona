// code by jph
package ch.alpine.ascona.filter;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.AbstractDemoHelper;

class Regularization2StepDemoTest {
  @Test
  public void testSimple() {
    AbstractDemoHelper.offscreen(new Regularization2StepDemo());
  }
}
