// code by jph
package ch.alpine.ascona.curve;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldPreferredWidth;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
import ch.alpine.bridge.win.AbstractDemo;
import ch.alpine.bridge.win.AxesRender;
import ch.alpine.sophus.lie.rn.RnGeodesic;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.alg.Transpose;
import ch.alpine.tensor.alg.UnitVector;
import ch.alpine.tensor.ext.Integers;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.itp.DeBoor;

public class DeBoorCustomDemo extends AbstractDemo {
  private static final ColorDataIndexed COLOR_DATA_INDEXED = ColorDataLists._097.cyclic().deriveWithAlpha(192);
  // ---
  @FieldPreferredWidth(200)
  public Tensor knots = Tensors.vector(0, 1);

  public DeBoorCustomDemo() {
    ToolbarFieldsEditor.add(this, timerFrame.jToolBar);
    // ---
    timerFrame.geometricComponent.addRenderInterface(AxesRender.INSTANCE);
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    graphics.setStroke(new BasicStroke(1.25f));
    {
      graphics.setFont(new Font(Font.DIALOG, Font.PLAIN, 10));
      try {
        Tensor domain = Subdivide.of(0, 1, 100);
        Tensor domahi = Subdivide.of(1, 2, 100);
        Tensor _knots = knots;
        if (Integers.isEven(_knots.length())) {
          int degree = _knots.length() >> 1;
          int length = degree + 1;
          // ---
          graphics.setColor(Color.LIGHT_GRAY);
          {
            Path2D path2d = geometricLayer.toPath2D(Tensors.fromString("{{0, 1}, {0, 0}, {1, 0}}"));
            graphics.setStroke(new BasicStroke(2f));
            graphics.setColor(Color.RED);
            graphics.draw(path2d);
          }
          for (int k_th = 0; k_th < length; ++k_th) {
            graphics.setColor(COLOR_DATA_INDEXED.getColor(k_th));
            DeBoor deBoor = DeBoor.of(RnGeodesic.INSTANCE, _knots, UnitVector.of(length, k_th));
            {
              graphics.setStroke(new BasicStroke(1.25f));
              Tensor values = domain.map(deBoor);
              Tensor tensor = Transpose.of(Tensors.of(domain, values));
              graphics.draw(geometricLayer.toPath2D(tensor));
            }
            {
              graphics.setStroke(new BasicStroke(1.25f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0));
              Tensor values = domahi.map(deBoor);
              Tensor tensor = Transpose.of(Tensors.of(domahi, values));
              graphics.draw(geometricLayer.toPath2D(tensor));
            }
          }
        }
      } catch (Exception exception) {
        exception.printStackTrace();
      }
    }
    graphics.setStroke(new BasicStroke(1f));
  }

  public static void main(String[] args) {
    new DeBoorCustomDemo().setVisible(1000, 800);
  }
}
