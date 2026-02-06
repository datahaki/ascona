// code by jph
package ch.alpine.ascona.crv.se2c;

import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascony.dis.Se2ClothoidDisplay;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.ren.PointsRender;
import ch.alpine.ascony.win.AbstractDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.d2.ex.Arrowhead;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Subdivide;

public class SpiralDemo extends AbstractDemo {
  private static final PointsRender POINTS_RENDER = //
      new PointsRender(new Color(128, 128, 128, 64), new Color(128, 128, 128, 128));
  private static final Tensor SEPARATORS = Subdivide.of(-3.0, 3.0, 50);

  @ReflectionMarker
  public static class Param {
    public SpiralParam spiralParam = SpiralParam.EULER;
  }

  private final Param param;

  public SpiralDemo() {
    this(new Param());
  }

  public SpiralDemo(Param param) {
    super(param);
    this.param = param;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    SpiralParam spiralParam = param.spiralParam;
    new PathRender(Color.BLUE, 1f).setCurve(spiralParam.points, false).render(geometricLayer, graphics);
    Tensor points = SEPARATORS.map(spiralParam.scalarTensorFunction);
    POINTS_RENDER.show(Se2ClothoidDisplay.ANALYTIC::matrixLift, Arrowhead.of(0.03), points) //
        .render(geometricLayer, graphics);
  }

  static void main() {
    launch();
  }
}
