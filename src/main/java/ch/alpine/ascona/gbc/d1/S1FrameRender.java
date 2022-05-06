// code by jph
package ch.alpine.ascona.gbc.d1;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import ch.alpine.ascona.util.api.Box2D;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.win.RenderInterface;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.lie.r2.CirclePoints;
import ch.alpine.tensor.sca.N;

/* package */ enum S1FrameRender implements RenderInterface {
  INSTANCE;

  private static final Tensor BOX = Box2D.CORNERS.multiply(RealScalar.of(2.5));
  private static final Tensor CIRCLE = CirclePoints.of(61).map(N.DOUBLE);

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setDefault(graphics);
    Path2D path2d = geometricLayer.toPath2D(BOX, true);
    graphics.setColor(Color.LIGHT_GRAY);
    graphics.draw(path2d);
    RenderQuality.setQuality(graphics);
    graphics.setColor(Color.LIGHT_GRAY);
    graphics.draw(geometricLayer.toPath2D(CIRCLE, true));
  }
}