// code by jph
package ch.alpine.ascona.util.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.sca.win.WindowFunctions;

class GeodesicCausalFiltersTest {
  @Test
  void testSimple() {
    for (ManifoldDisplay manifoldDisplay : ManifoldDisplays.ALL) {
      GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
      if (geodesicSpace instanceof HomogeneousSpace)
        for (WindowFunctions smoothingKernel : WindowFunctions.values())
          for (int radius = 0; radius < 3; ++radius)
            for (GeodesicCausalFilters geodesicCausalFilters : GeodesicCausalFilters.values()) {
              TensorUnaryOperator tensorUnaryOperator = geodesicCausalFilters.supply(manifoldDisplay, smoothingKernel.get(), radius, RationalScalar.HALF);
              assertNotNull(tensorUnaryOperator);
            }
    }
  }
}
