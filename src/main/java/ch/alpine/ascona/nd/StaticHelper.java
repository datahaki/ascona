// code by jph
package ch.alpine.ascona.nd;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import ch.alpine.ascony.api.Box2D;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophus.lie.se2.Se2Matrix;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;

/* package */ enum StaticHelper {
  ;
  public static void draw(CoordinateBoundingBox coordinateBoundingBox, GeometricLayer geometricLayer, Graphics2D graphics) {
    Path2D path2d = geometricLayer.toPath2D(Box2D.polygon(coordinateBoundingBox), true);
    graphics.setColor(new Color(0, 128, 0, 16));
    graphics.fill(path2d);
    graphics.setColor(new Color(128, 128, 128, 64));
    graphics.draw(path2d);
  }

  public static void draw(Tensor location, GeometricLayer geometricLayer, Graphics2D graphics) {
    geometricLayer.pushMatrix(Se2Matrix.translation(location));
    Point2D point2d = geometricLayer.toPoint2D(Tensors.vector(0, 0));
    graphics.setColor(new Color(255, 128, 128, 255));
    graphics.fillRect((int) point2d.getX() - 1, (int) point2d.getY() - 1, 4, 4);
    geometricLayer.popMatrix();
  }
}
