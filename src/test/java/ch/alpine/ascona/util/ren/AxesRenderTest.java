// code by jph
package ch.alpine.ascona.util.ren;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.io.ImageFormat;
import ch.alpine.tensor.mat.IdentityMatrix;

class AxesRenderTest {
  @Test
  void testSimple() {
    BufferedImage bi = ImageFormat.of(Array.zeros(100, 100, 4));
    Graphics2D graphics = bi.createGraphics();
    AxesRender.INSTANCE.render( //
        new GeometricLayer(IdentityMatrix.of(3)), graphics);
    graphics.dispose();
  }
}
