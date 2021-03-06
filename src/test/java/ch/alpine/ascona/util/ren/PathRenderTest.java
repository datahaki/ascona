// code by jph
package ch.alpine.ascona.util.ren;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.Color;

import org.junit.jupiter.api.Test;

import ch.alpine.tensor.Tensors;

class PathRenderTest {
  @Test
  void testFail() {
    PathRender pathRender = new PathRender(Color.BLACK);
    pathRender.setCurve(Tensors.fromString("{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}}"), false);
    assertThrows(Exception.class, () -> pathRender.render(null, null));
  }
}
