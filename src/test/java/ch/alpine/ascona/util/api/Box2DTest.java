// code by jph
package ch.alpine.ascona.util.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ch.alpine.sophus.crv.d2.PolygonArea;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;

class Box2DTest {
  @Test
  void testSquare() {
    Tensor SQUARE = Tensors.fromString("{{0, 0}, {1, 0}, {1, 1}, {0, 1}}").unmodifiable();
    assertEquals(SQUARE, Box2D.SQUARE);
    assertEquals(PolygonArea.of(SQUARE), RealScalar.ONE);
  }

  @Test
  void testCorners() {
    Tensor CORNERS = Tensors.fromString("{{-1, -1}, {1, -1}, {1, 1}, {-1, 1}}").unmodifiable();
    assertEquals(CORNERS, Box2D.CORNERS);
    assertEquals(PolygonArea.of(CORNERS), RealScalar.of(4));
  }
}
