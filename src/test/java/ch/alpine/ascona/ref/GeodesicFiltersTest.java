// code by jph
package ch.alpine.ascona.ref;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.dat.GokartPos;
import ch.alpine.ascona.dat.GokartPosVel;
import ch.alpine.ascony.api.GeodesicFilters;
import ch.alpine.ascony.dat.Se2Pos;
import ch.alpine.ascony.dat.Se2PosHz;
import ch.alpine.ascony.dat.Se2PosVelHz;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.Se2Display;
import ch.alpine.sophis.flt.CenterFilter;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.lie.so2.So2;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.nrm.MatrixInfinityNorm;
import ch.alpine.tensor.qty.Timing;
import ch.alpine.tensor.sca.Chop;
import ch.alpine.tensor.sca.win.WindowFunctions;

class GeodesicFiltersTest {
  @Test
  void testSimple() {
    List<String> lines = GokartPos.INSTANCE.keys();
    Se2PosHz posHz = GokartPos.INSTANCE.get(lines.getFirst(), 250); // limit , 250
    Se2Pos control = posHz.se2Pos();
    ManifoldDisplay manifoldDisplay = Se2Display.INSTANCE;
    HomogeneousSpace homogeneousSpace = manifoldDisplay.homogeneousSpace();
    ScalarUnaryOperator smoothingKernel = WindowFunctions.GAUSSIAN.get();
    int radius = 7;
    Map<GeodesicFilters, Tensor> map = new EnumMap<>(GeodesicFilters.class);
    for (GeodesicFilters geodesicFilters : GeodesicFilters.values()) {
      TensorUnaryOperator tensorUnaryOperator = //
          geodesicFilters.supply(homogeneousSpace, smoothingKernel);
      CenterFilter centerFilter = new CenterFilter(tensorUnaryOperator, radius);
      Tensor filtered = centerFilter.apply(control.getGeodesicControlPoints(manifoldDisplay));
      map.put(geodesicFilters, filtered);
    }
    for (GeodesicFilters lieGroupFilters : GeodesicFilters.values()) {
      Tensor diff = map.get(lieGroupFilters).subtract(map.get(GeodesicFilters.BIINVARIANT_MEAN));
      diff.set(So2.MOD, Tensor.ALL, 2);
      Scalar norm = MatrixInfinityNorm.of(diff);
      Chop._02.requireZero(norm);
    }
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
