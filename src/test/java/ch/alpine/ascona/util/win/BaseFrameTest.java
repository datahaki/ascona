// code by jph
package ch.alpine.ascona.util.win;

import org.junit.jupiter.api.Test;

class BaseFrameTest {
  @Test
  void testQuick() {
    BaseFrame bf = new BaseFrame();
    bf.jFrame.setVisible(true);
    bf.offscreen();
    bf.geometricComponent.setOffset(2, 3);
    bf.close();
  }
}
