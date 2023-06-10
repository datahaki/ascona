// code by jph
package ch.alpine.ascona.util.ren;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.TensorUnaryOperator;

// TODO ASCONA API misnomer && code not effective
public record AreaRender(Color color_fill, TensorUnaryOperator matrixLift, Tensor shape, Tensor points) implements RenderInterface {
  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    for (Tensor point : points) {
      geometricLayer.pushMatrix(matrixLift.apply(point));
      Path2D path2d = geometricLayer.toPath2D(shape);
      path2d.closePath();
      graphics.setColor(color_fill);
      graphics.fill(path2d);
      geometricLayer.popMatrix();
    }
  }
}
