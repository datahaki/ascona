// code by jph
package ch.alpine.ascona.util.ren;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import ch.alpine.ascona.util.win.RenderInterface;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.TensorUnaryOperator;

public record PointsRender(Color color_fill, Color color_draw) {

  public RenderInterface show(TensorUnaryOperator matrixLift, Tensor shape, Tensor points) {
    return new Show(matrixLift, shape, points);
  }
  private class Show implements RenderInterface {
    private final TensorUnaryOperator matrixLift;
    private final Tensor shape;
    private final Tensor points;

    public Show(TensorUnaryOperator matrixLift, Tensor shape, Tensor points) {
      this.matrixLift = matrixLift;
      this.shape = shape;
      this.points = points;
    }

    @Override
    public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
      for (Tensor point : points) {
        geometricLayer.pushMatrix(matrixLift.apply(point));
        Path2D path2d = geometricLayer.toPath2D(shape);
        path2d.closePath();
        graphics.setColor(color_fill);
        graphics.fill(path2d);
        graphics.setColor(color_draw);
        graphics.draw(path2d);
        geometricLayer.popMatrix();
      }
    }
  }
}
