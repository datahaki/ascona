// code by jph
package ch.alpine.ascona.ref;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import ch.alpine.ascona.dat.GokartPosVel;
import ch.alpine.ascony.dat.Se2PosVelHz;
import ch.alpine.ascony.dis.Se2Display;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.ArrayQ;
import ch.alpine.tensor.qty.Quantity;
import ch.alpine.tensor.sca.Abs;

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
}
