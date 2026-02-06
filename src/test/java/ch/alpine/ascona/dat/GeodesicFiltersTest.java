// code by jph
package ch.alpine.ascona.dat;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ch.alpine.ascony.api.GeodesicFilters;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.Se2Display;
import ch.alpine.sophis.flt.CenterFilter;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.lie.so2.So2;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.ext.Timing;
import ch.alpine.tensor.nrm.MatrixInfinityNorm;
import ch.alpine.tensor.sca.Chop;
import ch.alpine.tensor.sca.win.WindowFunctions;

class GeodesicFiltersTest {
  private static void _check(GokartPos gokartPoseData) {
    List<String> lines = gokartPoseData.list();
    Tensor control = gokartPoseData.getData(lines.get(0)); // limit , 250
    ManifoldDisplay manifoldDisplay = Se2Display.INSTANCE;
    HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
    ScalarUnaryOperator smoothingKernel = WindowFunctions.GAUSSIAN.get();
    int radius = 7;
    Map<GeodesicFilters, Tensor> map = new EnumMap<>(GeodesicFilters.class);
    for (GeodesicFilters geodesicFilters : GeodesicFilters.values()) {
      TensorUnaryOperator tensorUnaryOperator = //
          geodesicFilters.supply(homogeneousSpace, smoothingKernel);
      Tensor filtered = new CenterFilter(tensorUnaryOperator, radius).apply(control);
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
  void testSimple() {
    _check(new GokartPos());
    // _check(GokartPosVel.INSTANCE); // TODO
  }

  @Test
  void testTiming() {
    String name = "50Hz/20190701T170957_06.csv";
    Tensor control = new GokartPosVel().getData(name); // TODO
    ManifoldDisplay manifoldDisplay = Se2Display.INSTANCE;
    HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
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
