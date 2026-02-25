// code by jph
package ch.alpine.ascona.misc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;

class VehicleStaticTest {
  @Test
  void test() {
    BufferedImage bufferedImage_c = VehicleStatic.INSTANCE.bufferedImage_c();
    assertNotNull(bufferedImage_c);
  }

  @Test
  void testVisibility() {
    assertFalse(Modifier.isPublic(VehicleStatic.class.getModifiers()));
  }
}
