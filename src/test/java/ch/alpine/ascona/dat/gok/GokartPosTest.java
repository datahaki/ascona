// code by jph
package ch.alpine.ascona.dat.gok;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import ch.alpine.ascony.win.ControlPointsSe2;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.alg.ArrayQ;
import ch.alpine.tensor.qty.Quantity;
import ch.alpine.tensor.sca.Abs;

class GokartPosTest {
  static Stream<String> list() {
    return GokartPos.INSTANCE.keys().stream();
  }

  @ParameterizedTest
  @MethodSource("list")
  void testImport(String key) {
    PosHz posHz = GokartPos.INSTANCE.get(key, 1_000_000);
    ControlPointsSe2 tensor = posHz.getPoseSequence();
    ArrayQ.require(tensor.points_se2());
    Scalar scalar = posHz.getSamplingRate();
    String number = key.substring(key.indexOf('/') + 1, key.indexOf("Hz"));
    Scalar folder = Scalars.fromString(number + "[Hz]");
    assertTrue(Scalars.lessThan(Abs.between(scalar, folder), Quantity.of(2, "Hz")));
  }
}
