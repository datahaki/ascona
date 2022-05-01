// code by jph
package ch.alpine.ascona.ref.d1h;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.AbstractDemoHelper;

class S2HermiteSubdivisionDemoTest {
  @Test
  public void testSimple() {
    AbstractDemoHelper.offscreen(new S2HermiteSubdivisionDemo());
  }
}
