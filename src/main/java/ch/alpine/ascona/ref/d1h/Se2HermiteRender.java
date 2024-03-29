// code by jph
package ch.alpine.ascona.ref.d1h;

import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascona.util.ren.RenderInterface;
import ch.alpine.bridge.col.Hsluv;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.GfxMatrix;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.lie.Cross;

/* package */ record Se2HermiteRender(Tensor points, Scalar scale) implements RenderInterface {
  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    for (Tensor point : points) {
      geometricLayer.pushMatrix(GfxMatrix.of(point.get(0)));
      Tensor pv = point.get(1);
      Color color = Hsluv.of(pv.Get(2).number().doubleValue() * 0.3, 1, 0.5, 0.5);
      graphics.setColor(color);
      Tensor vec = Cross.of(pv.extract(0, 2).multiply(scale));
      graphics.draw(geometricLayer.toLine2D(vec));
      geometricLayer.popMatrix();
    }
  }
}
