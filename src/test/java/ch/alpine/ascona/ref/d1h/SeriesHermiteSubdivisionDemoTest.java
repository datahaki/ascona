// code by jph
package ch.alpine.ascona.ref.d1h;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.AbstractDemoHelper;

class SeriesHermiteSubdivisionDemoTest {
  @Test
  public void testSimple() {
    AbstractDemoHelper.offscreen(new SeriesHermiteSubdivisionDemo());
  }
}
