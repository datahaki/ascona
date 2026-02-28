// code by jph
package ch.alpine.ascona.crv;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SymMaskImagesTest {
  @Test
  void testSimple() {
    SymMaskImages[] values = SymMaskImages.values();
    assertTrue(5 < values.length);
  }
}
