// code by jph
package ch.alpine.ascona.ref.d1;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SymMaskImagesTest {
  @Test
  public void testSimple() {
    SymMaskImages[] values = SymMaskImages.values();
    assertTrue(5 < values.length);
  }
}
