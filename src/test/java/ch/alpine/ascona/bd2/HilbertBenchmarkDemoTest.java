// code by jph
package ch.alpine.ascona.bd2;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.AbstractDemoHelper;

class HilbertBenchmarkDemoTest {
  @Test
  public void testSimple() {
    AbstractDemoHelper.offscreen(new HilbertBenchmarkDemo());
  }
}
