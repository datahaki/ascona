// code by jph
package ch.alpine.ascona.ref.d2;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.AbstractDemoHelper;

class SurfaceMeshDemoTest {
  @Test
  public void testSimple() {
    AbstractDemoHelper.offscreen(new SurfaceMeshDemo());
  }
}
