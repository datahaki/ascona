// code by jph
package ch.alpine.ascona.euclid;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import ch.alpine.ascony.ren.ColorStrokeIndexed;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.EuclideanPlaneDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.lie.se2.Se2Matrix;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.col.ColorDataIndexed;
import ch.alpine.tensor.col.ColorDataLists;
import ch.alpine.tensor.lie.rot.Cross;
import ch.alpine.tensor.nrm.Vector2Norm;
import ch.alpine.tensor.pdf.Distribution;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.UniformDistribution;

class LaserTagDemo extends EuclideanPlaneDemo {
  private static final ColorDataIndexed COLOR_DATA_INDEXED = ColorDataLists._097.strict().deriveWithAlpha(128);

  @ReflectionMarker
  static class Param {
    public Boolean show = true;
    public String TEXT = "WILLKOMMEN IN";
  }

  private final Param param;

  public LaserTagDemo() {
    super(param = new Param());
    // ---
    Distribution distribution = UniformDistribution.of(-4, 4);
    setControlPointsSe2(RandomVariate.of(distribution, param.TEXT.length() + 2, 3));
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.HEAD_TAIL;
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor control = getGeodesicControlPoints();
    new PathRender(ColorStrokeIndexed._097.getColorStroke(0), //
        param.show ? control : control.extract(0, 3), false).render(geometricLayer, graphics);
    graphics.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
    for (int index = 1; index < control.length() - 1; ++index) {
      Tensor p = control.get(index - 1);
      Tensor q = control.get(index);
      Tensor r = control.get(index + 1);
      Tensor d1 = Vector2Norm.NORMALIZE.apply(p.subtract(q));
      Tensor d2 = Vector2Norm.NORMALIZE.apply(r.subtract(q));
      Tensor o1 = Vector2Norm.NORMALIZE.apply(d1.add(d2));
      Tensor o2 = Cross.of(o1);
      geometricLayer.pushMatrix(Se2Matrix.translation(q));
      Tensor polygon = Tensors.of(o2, o1.negate(), o2.negate());
      Path2D path2d = geometricLayer.toPath2D(polygon);
      graphics.setColor(COLOR_DATA_INDEXED.getColor(1));
      graphics.draw(path2d);
      graphics.setColor(COLOR_DATA_INDEXED.getColor(1));
      graphics.fill(path2d);
      Point2D point2d = geometricLayer.toPoint2D(o1.multiply(RealScalar.of(-0.5)));
      graphics.setColor(Color.DARK_GRAY);
      graphics.drawString("" + param.TEXT.charAt(index - 1), (int) point2d.getX() - 8, (int) point2d.getY() + 10);
      geometricLayer.popMatrix();
    }
  }

  static void main() {
    new LaserTagDemo().runStandalone();
  }
}
