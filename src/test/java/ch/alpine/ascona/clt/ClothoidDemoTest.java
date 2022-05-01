// code by jph
package ch.alpine.ascona.clt;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.AbstractDemoHelper;

class ClothoidDemoTest {
  @Test
  public void testSimple() {
    AbstractDemoHelper.offscreen(new ClothoidDemo());
  }
}
