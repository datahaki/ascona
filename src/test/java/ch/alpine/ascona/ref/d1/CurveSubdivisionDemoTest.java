// code by jph
package ch.alpine.ascona.ref.d1;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.AbstractDemoHelper;

class CurveSubdivisionDemoTest {
  @Test
  public void testSimple() {
    AbstractDemoHelper.offscreen(new CurveSubdivisionDemo());
  }
}
