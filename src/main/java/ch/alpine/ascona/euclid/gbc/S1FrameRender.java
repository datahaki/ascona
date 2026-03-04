// code by jph
package ch.alpine.ascona.euclid.gbc;

import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.RenderInterface;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.lie.rot.CirclePoints;
import ch.alpine.tensor.sca.N;

/* package */ enum S1FrameRender implements RenderInterface {
  INSTANCE;

  private static final Tensor CIRCLE = CirclePoints.of(61).maps(N.DOUBLE);

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    graphics.setColor(Color.LIGHT_GRAY);
    graphics.draw(geometricLayer.toPath2D(CIRCLE, true));
  }
}
