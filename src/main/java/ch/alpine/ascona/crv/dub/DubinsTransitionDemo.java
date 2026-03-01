// code by jph
package ch.alpine.ascona.crv.dub;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.util.List;

import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.GridRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointTypes;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophis.crv.dub.DubinsPath;
import ch.alpine.sophis.crv.dub.DubinsPathGenerator;
import ch.alpine.sophis.crv.dub.DubinsType;
import ch.alpine.sophis.crv.dub.FixedRadiusDubins;
import ch.alpine.sophis.ts.DubinsTransition;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;

class DubinsTransitionDemo extends ControlPointsDemo {
  private static final ColorDataIndexed COLOR_DATA_INDEXED = ColorDataLists._097.cyclic();

  public DubinsTransitionDemo() {
    setControlPointsSe2(Tensors.fromString("{{0, 0, 0}, {3, 0, 0}}"));
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.SE2_ONLY;
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointTypes.HEAD_TAIL;
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    new GridRender(timerFrame.geometricComponent.jComponent::getSize).render(geometricLayer, graphics);
    Tensor controlPointsSe2 = getControlPointsSe2();
    Tensor START = controlPointsSe2.get(0);
    Tensor mouse = controlPointsSe2.get(1);
    // ---
    DubinsPathGenerator dubinsPathGenerator = FixedRadiusDubins.of(START, mouse, RealScalar.of(1));
    List<DubinsPath> list = dubinsPathGenerator.stream().toList();
    Scalar minResolution = geometricLayer.pixel2modelFactor(RealScalar.of(5));
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
