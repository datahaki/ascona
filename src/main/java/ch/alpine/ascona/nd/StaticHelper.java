// code by jph
package ch.alpine.ascona.nd;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Clip;

/* package */ enum StaticHelper {
  ;
  /** @param coordinateBoundingBox
   * @return polygon defined by the corners of the first two dimensions of given
   * coordinateBoundingBox */
  public static Tensor polygon(CoordinateBoundingBox coordinateBoundingBox) {
    Clip clip0 = coordinateBoundingBox.getClip(0);
    Clip clip1 = coordinateBoundingBox.getClip(1);
    Tensor c00 = Tensors.of(clip0.min(), clip1.min());
    Tensor c01 = Tensors.of(clip0.min(), clip1.max());
    Tensor c11 = Tensors.of(clip0.max(), clip1.max());
    Tensor c10 = Tensors.of(clip0.max(), clip1.min());
    return Unprotect.byRef( //
        c00, //
        c01, //
        c11, //
        c10);
  }

  public static void draw(CoordinateBoundingBox coordinateBoundingBox, GeometricLayer geometricLayer, Graphics2D graphics) {
    Path2D path2d = geometricLayer.toPath2D(polygon(coordinateBoundingBox), true);
    graphics.setColor(new Color(0, 128, 0, 16));
    graphics.fill(path2d);
    graphics.setColor(new Color(128, 128, 128, 64));
    graphics.draw(path2d);
  }

  public static void draw(Tensor location, GeometricLayer geometricLayer, Graphics2D graphics) {
    geometricLayer.pushMatrix(GfxMatrix.translation(location));
    Point2D point2d = geometricLayer.toPoint2D(Tensors.vector(0, 0));
    graphics.setColor(new Color(255, 128, 128, 255));
    graphics.fillRect((int) point2d.getX() - 1, (int) point2d.getY() - 1, 4, 4);
    geometricLayer.popMatrix();
  }
}
