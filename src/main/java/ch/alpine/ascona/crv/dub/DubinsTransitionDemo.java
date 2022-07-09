// code by jph
package ch.alpine.ascona.crv.dub;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.util.List;
import java.util.stream.Collectors;

import ch.alpine.ascona.util.ren.AxesRender;
import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophus.crv.dub.DubinsPath;
import ch.alpine.sophus.crv.dub.DubinsPathGenerator;
import ch.alpine.sophus.crv.dub.DubinsTransition;
import ch.alpine.sophus.crv.dub.FixedRadiusDubins;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Array;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;

// TODO ASCONA blend with dubins path demo
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
    List<DubinsPath> list = dubinsPathGenerator.stream().collect(Collectors.toList());
    Scalar minResolution = RealScalar.of(geometricLayer.pixel2modelWidth(5));
    {
      graphics.setColor(COLOR_DATA_INDEXED.getColor(0));
      graphics.setStroke(new BasicStroke(1));
      for (DubinsPath dubinsPath : list) {
        DubinsTransition dubinsTransition = new DubinsTransition(START, mouse, dubinsPath);
        graphics.draw(geometricLayer.toPath2D(dubinsTransition.linearized(minResolution)));
      }
    }
  }

  public static void main(String[] args) {
    launch();
  }
}
