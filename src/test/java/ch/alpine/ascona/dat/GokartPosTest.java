// code by jph
package ch.alpine.ascona.dat;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import ch.alpine.ascony.api.GeodesicFilters;
import ch.alpine.ascony.dat.Se2Pos;
import ch.alpine.ascony.dat.Se2PosHz;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.Se2Display;
import ch.alpine.sophis.flt.CenterFilter;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.lie.so2.So2;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.ArrayQ;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.nrm.MatrixInfinityNorm;
import ch.alpine.tensor.qty.Quantity;
import ch.alpine.tensor.sca.Abs;
import ch.alpine.tensor.sca.Chop;
import ch.alpine.tensor.sca.win.WindowFunctions;

class GokartPosTest {
  static Stream<String> list() {
    return GokartPos.INSTANCE.keys().stream();
  }

  @ParameterizedTest
  @MethodSource("list")
  void testImport(String key) {
    Se2PosHz posHz = GokartPos.INSTANCE.get(key, 1_000_000);
    Se2Pos tensor = posHz.se2Pos();
    ArrayQ.require(tensor.points_se2());
    Scalar scalar = posHz.samplingRate();
    String number = key.substring(key.indexOf('/') + 1, key.indexOf("Hz"));
    Scalar folder = Scalars.fromString(number + "[Hz]");
    assertTrue(Scalars.lessThan(Abs.between(scalar, folder), Quantity.of(2, "Hz")));
  }

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
}
