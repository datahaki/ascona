// code by jph
package ch.alpine.ascona.dat;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import ch.alpine.ascony.api.GeodesicFilters;
import ch.alpine.ascony.dat.Se2PosVelHz;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.Se2Display;
import ch.alpine.sophis.flt.CenterFilter;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.ArrayQ;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.qty.Quantity;
import ch.alpine.tensor.qty.Timing;
import ch.alpine.tensor.sca.Abs;
import ch.alpine.tensor.sca.win.WindowFunctions;

class GokartPosVelTest {
  static Stream<String> list() {
    return GokartPosVel.INSTANCE.keys().stream();
  }

  @ParameterizedTest
  @MethodSource("list")
  void testImport(String key) {
    Se2PosVelHz posHz = GokartPosVel.INSTANCE.get(key, 1_000_000);
    Tensor tensor = posHz.se2PosVel().getHermiteControlPoints(Se2Display.INSTANCE);
    ArrayQ.require(tensor);
    Scalar scalar = posHz.samplingRate();
    String number = key.substring(0, key.indexOf("Hz"));
    Scalar folder = Scalars.fromString(number + "[Hz]");
    assertTrue(Scalars.lessThan(Abs.between(scalar, folder), Quantity.of(2, "Hz")));
  }

  @Test
  void testTiming() {
    String name = "50Hz/20190701T170957_06.csv";
    Se2PosVelHz controlPosVelSe2Hz = GokartPosVel.INSTANCE.get(name, 100_000);
    Tensor control = controlPosVelSe2Hz.se2PosVel().getHermiteControlPoints(Se2Display.INSTANCE);
    control = control.get(Tensor.ALL, 0); // keep only position
    ManifoldDisplay manifoldDisplay = Se2Display.INSTANCE;
    HomogeneousSpace homogeneousSpace = manifoldDisplay.homogeneousSpace();
    ScalarUnaryOperator smoothingKernel = WindowFunctions.GAUSSIAN.get();
    for (int radius : new int[] { 0, 10 }) {
      for (GeodesicFilters geodesicFilters : GeodesicFilters.values()) {
        TensorUnaryOperator tensorUnaryOperator = //
            geodesicFilters.supply(homogeneousSpace, smoothingKernel);
        Timing timing = Timing.started();
        new CenterFilter(tensorUnaryOperator, radius).apply(control);
        timing.stop();
        // System.out.println(lieGroupFilters+" "+timing.seconds());
      }
    }
  }
}
