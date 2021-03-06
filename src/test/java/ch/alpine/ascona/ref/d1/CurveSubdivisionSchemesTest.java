// code by jph
package ch.alpine.ascona.ref.d1;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CurveSubdivisionSchemesTest {
  @Test
  void testSimple() {
    int count = 0;
    for (CurveSubdivisionSchemes curveSubdivisionSchemes : CurveSubdivisionSchemes.values())
      if (curveSubdivisionSchemes.isStringSupported())
        ++count;
    assertTrue(10 < count);
  }
}
