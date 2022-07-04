package ch.alpine.ascona.flt;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.util.win.AbstractDemoHelper;

class BiinvariantMeanDemoTest {
  @Test
  void test() {
    AbstractDemoHelper.offscreen(new BiinvariantMeanDemo());
  }
}
