// code by jph
package ch.alpine.ascona.ext;

import org.junit.jupiter.api.Test;

import ch.alpine.ascona.misc.VehicleStatic;

class VehicleStaticTest {
  @Test
  void test() {
    VehicleStatic.INSTANCE.getClass();
  }
}
