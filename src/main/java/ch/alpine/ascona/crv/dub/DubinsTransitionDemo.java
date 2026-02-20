// code by jph
package ch.alpine.ascona.crv.dub;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.util.List;

import ch.alpine.ascony.ren.AxesRender;
import ch.alpine.ascony.win.AbstractDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophis.crv.dub.DubinsPath;
import ch.alpine.sophis.crv.dub.DubinsPathGenerator;
import ch.alpine.sophis.crv.dub.DubinsType;
import ch.alpine.sophis.crv.dub.FixedRadiusDubins;
import ch.alpine.sophis.ts.DubinsTransition;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;

public class DubinsTransitionDemo extends AbstractDemo {
  private static final Tensor START = Array.zeros(3).unmodifiable();
  private static final ColorDataIndexed COLOR_DATA_INDEXED = ColorDataLists._097.cyclic();

  public DubinsTransitionDemo() {
    super(new Object());
    timerFrame.geometricComponent.addRenderInterfaceBackground(AxesRender.INSTANCE);
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    Tensor mouse = timerFrame.geometricComponent.getMouseSe2CState();
    // ---
    DubinsPathGenerator dubinsPathGenerator = FixedRadiusDubins.of(START, mouse, RealScalar.of(1));
    List<DubinsPath> list = dubinsPathGenerator.stream().toList();
    Scalar minResolution = RealScalar.of(geometricLayer.pixel2modelWidth(5));
    {
      graphics.setStroke(new BasicStroke(1));
      for (DubinsPath dubinsPath : list) {
        DubinsType dubinsType = dubinsPath.dubinsType();
        graphics.setColor(COLOR_DATA_INDEXED.getColor(dubinsType.ordinal()));
        DubinsTransition dubinsTransition = new DubinsTransition(START, mouse, dubinsPath);
        graphics.draw(geometricLayer.toPath2D(dubinsTransition.linearized(minResolution)));
      }
    }
  }

  static void main() {
    new DubinsTransitionDemo().runStandalone();
  }
}
