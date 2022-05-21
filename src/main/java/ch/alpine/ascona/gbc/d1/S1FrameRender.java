// code by jph
package ch.alpine.ascona.gbc.d1;

import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascona.util.win.RenderInterface;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.lie.r2.CirclePoints;
import ch.alpine.tensor.sca.N;

/* package */ enum S1FrameRender implements RenderInterface {
  INSTANCE;

  private static final Tensor CIRCLE = CirclePoints.of(61).map(N.DOUBLE);

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    graphics.setColor(Color.LIGHT_GRAY);
    graphics.draw(geometricLayer.toPath2D(CIRCLE, true));
  }
}
