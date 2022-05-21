// code by jph
package ch.alpine.ascona.util.ren;

import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascona.util.api.Box2D;
import ch.alpine.ascona.util.win.RenderInterface;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;

public record BoundingBoxRender(CoordinateBoundingBox coordinateBoundingBox) implements RenderInterface {
  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    graphics.setColor(Color.LIGHT_GRAY);
    graphics.draw(geometricLayer.toPath2D(Box2D.polygon(coordinateBoundingBox), true));
  }
}
