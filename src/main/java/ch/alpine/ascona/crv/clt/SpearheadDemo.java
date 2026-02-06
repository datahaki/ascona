// code by jph
package ch.alpine.ascona.crv.clt;

import java.awt.Graphics2D;

import ch.alpine.ascony.api.Spearhead;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.AxesRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;

public class SpearheadDemo extends ControlPointsDemo {
  private static final ColorDataIndexed COLOR_DATA_INDEXED = ColorDataLists._097.cyclic().deriveWithAlpha(128);

  public SpearheadDemo() {
    super(new AsconaParam(false, ManifoldDisplays.SE2_ONLY));
    // ---
    timerFrame.geometricComponent.addRenderInterface(AxesRender.INSTANCE);
    // ---
    setControlPointsSe2(Tensors.fromString("{{-0.5, -0.5, 0.3}}"));
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    Tensor control = getGeodesicControlPoints();
    controlPointsRender.render(geometricLayer, graphics);
    Tensor curve = Spearhead.of(control.get(0), RealScalar.of(geometricLayer.pixel2modelWidth(10)));
    graphics.setColor(COLOR_DATA_INDEXED.getColor(1));
    graphics.fill(geometricLayer.toPath2D(curve));
    new PathRender(COLOR_DATA_INDEXED.getColor(0), 1.5f) //
        .setCurve(curve, false) //
        .render(geometricLayer, graphics);
  }

  static void main() {
    launch();
  }
}
