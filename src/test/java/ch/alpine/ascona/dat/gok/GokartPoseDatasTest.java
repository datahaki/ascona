package ch.alpine.ascona.dat.gok;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ch.alpine.bridge.gfx.PvmBuilder;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;

class GokartPoseDatasTest {
  @Test
  void test() {
    Tensor HANGAR_MODEL2PIXEL = //
        Tensors.fromString("{{7.5, 0, 100}, {0, -7.5, 800}, {0, 0, 1}}");
    Tensor pvm = PvmBuilder.rhs().setOffset(100, 800).setPerPixel(7.5).digest();
    assertEquals(HANGAR_MODEL2PIXEL, pvm);
  }
}
