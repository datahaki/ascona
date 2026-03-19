// code by jph
package ch.alpine.ascona.ref;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import ch.alpine.ascona.dat.GokartPos;
import ch.alpine.ascony.dat.Se2Pos;
import ch.alpine.ascony.dat.Se2PosHz;
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
    Se2PosHz posHz = GokartPos.INSTANCE.get(key, 1_000_000);
    Se2Pos tensor = posHz.se2Pos();
    ArrayQ.require(tensor.points_se2());
    Scalar scalar = posHz.samplingRate();
    String number = key.substring(key.indexOf('/') + 1, key.indexOf("Hz"));
    Scalar folder = Scalars.fromString(number + "[Hz]");
    assertTrue(Scalars.lessThan(Abs.between(scalar, folder), Quantity.of(2, "Hz")));
  }
}
