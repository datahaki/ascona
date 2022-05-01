// code by jph
package ch.alpine.ascona.ext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.api.GeodesicCausalFilters;
import ch.alpine.ascona.dis.ManifoldDisplay;
import ch.alpine.ascona.dis.ManifoldDisplays;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.sca.win.WindowFunctions;

class GeodesicCausalFiltersTest {
  @Test
  public void testSimple() {
    for (ManifoldDisplay manifoldDisplay : ManifoldDisplays.LIE_GROUPS)
      for (WindowFunctions smoothingKernel : WindowFunctions.values())
        for (int radius = 0; radius < 3; ++radius)
          for (GeodesicCausalFilters geodesicCausalFilters : GeodesicCausalFilters.values()) {
            TensorUnaryOperator tensorUnaryOperator = geodesicCausalFilters.supply(manifoldDisplay, smoothingKernel.get(), radius, RationalScalar.HALF);
            assertNotNull(tensorUnaryOperator);
          }
  }
}
