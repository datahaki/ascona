// code by jph
package ch.alpine.ascona.crv.se2c;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import ch.alpine.ascony.dis.Se2ClothoidDisplay;
import ch.alpine.ascony.ren.AxesRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.ren.PointsRender;
import ch.alpine.ascony.win.AbstractDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.fig.ParametricPlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.d2.ex.Arrowhead;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;

public class SpiralDemo extends AbstractDemo {
  private static final PointsRender POINTS_RENDER = //
      new PointsRender(new Color(128, 128, 128, 64), new Color(128, 128, 128, 128));

  @ReflectionMarker
  public static class Param {
    public SpiralParam spiralParam = SpiralParam.EULER;
    public Clip clip = Clips.absolute(10);
    public Integer samples = 5000;
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
    AxesRender.INSTANCE.render(geometricLayer, graphics);
    SpiralParam spiralParam = param.spiralParam;
    Clip clip = param.clip;
    {
      Tensor points = Subdivide.increasing(clip, param.samples).maps(spiralParam.scalarTensorFunction);
      new PathRender(Color.BLUE, 1f).setCurve(points, false).render(geometricLayer, graphics);
    }
    {
      Tensor points = Subdivide.increasing(clip, 50).maps(spiralParam.scalarTensorFunction);
      POINTS_RENDER.show(Se2ClothoidDisplay.ANALYTIC::matrixLift, Arrowhead.of(0.03), points) //
          .render(geometricLayer, graphics);
    }
    graphics.drawString(spiralParam.scalarTensorFunction.toString(), 0, 20);
    {
      Show show = new Show();
      show.setPlotLabel(param.spiralParam.toString());
      show.add(ParametricPlot.of(s -> spiralParam.scalarTensorFunction.apply(s).extract(0, 2), clip));
      Dimension dimension = timerFrame.geometricComponent.jComponent.getSize();
      show.setAspectRatioOne();
      show.render_autoIndent(graphics, new Rectangle(dimension.width - 400, 0, 400, 400));
    }
  }

  static void main() {
    launch();
  }
}
