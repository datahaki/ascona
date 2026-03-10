// code by jph
package ch.alpine.ascona.usr;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.function.Function;

import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.RenderInterface;
import ch.alpine.tensor.Tensor;

record MatRender(Function<GeometricLayer, Tensor> supplier) implements RenderInterface {
  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor tensor = supplier.apply(geometricLayer);
    graphics.setColor(Color.BLACK);
    graphics.setFont(new Font(Font.DIALOG, Font.PLAIN, 13));
    int index = 5;
    for (Tensor row : tensor) {
      ++index;
      graphics.drawString("" + row, 100, index * 20);
    }
  }
}
