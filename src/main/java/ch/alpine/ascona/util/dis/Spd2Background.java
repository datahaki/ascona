// code by jph
package ch.alpine.ascona.util.dis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

import ch.alpine.ascona.util.win.RenderInterface;
import ch.alpine.bridge.gfx.GeometricLayer;

/* package */ enum Spd2Background implements RenderInterface {
  INSTANCE;

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Point2D point2d = geometricLayer.toPoint2D(0, 0);
    graphics.setColor(Color.DARK_GRAY);
    graphics.fill(new Rectangle( //
        (int) point2d.getX(), //
        (int) point2d.getY(), 1, 1));
  }
}
