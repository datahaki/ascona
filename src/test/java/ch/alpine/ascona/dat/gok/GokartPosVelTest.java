// code by jph
package ch.alpine.ascona.dat.gok;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Scalars;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.ArrayQ;
import ch.alpine.tensor.qty.Quantity;
import ch.alpine.tensor.sca.Abs;

class GokartPosVelTest {
  static Stream<String> list() {
    return GokartPosVel.keys().stream();
  }

  @ParameterizedTest
  @MethodSource("list")
  void testImport(String key) {
    PosVelHz posHz = GokartPosVel.get(key, 1_000_000);
    Tensor tensor = posHz.getPosVelSequence();
    ArrayQ.require(tensor);
    Scalar scalar = posHz.getSamplingRate();
    String number = key.substring(0, key.indexOf("Hz"));
    Scalar folder = Scalars.fromString(number + "[Hz]");
    assertTrue(Scalars.lessThan(Abs.between(scalar, folder), Quantity.of(2, "Hz")));
  }
}
