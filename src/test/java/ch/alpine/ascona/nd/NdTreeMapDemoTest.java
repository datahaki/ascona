// code by jph
package ch.alpine.ascona.nd;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.AbstractDemoHelper;

class NdTreeMapDemoTest {
  @Test
  public void testSimple() {
    AbstractDemoHelper.offscreen(new NdTreeMapDemo());
  }
}
