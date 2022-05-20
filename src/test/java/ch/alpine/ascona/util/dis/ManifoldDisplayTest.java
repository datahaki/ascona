// code by jph
package ch.alpine.ascona.util.dis;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ManifoldDisplayTest {
  @Test
  public void testHs() {
    for (ManifoldDisplay manifoldDisplay : ManifoldDisplays.METRIC)
      assertNotNull(manifoldDisplay.biinvariant());
  }
}
