// code by jph
package ch.alpine.ascona.misc;

import java.awt.Graphics2D;

import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.AxesRender;
import ch.alpine.ascona.util.win.ControlPointsDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.tmp.ResamplingMethod;
import ch.alpine.tensor.tmp.ResamplingMethods;

/** split interface and biinvariant mean based curve subdivision */
public class PlotDemo extends ControlPointsDemo {
  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(true, ManifoldDisplays.R2_ONLY);
    }

    public ResamplingMethod rm = ResamplingMethods.LINEAR_INTERPOLATION;
    public Integer refine = 5;
  }

  private final Param param;

  public PlotDemo() {
    this(new Param());
  }

  public PlotDemo(Param param) {
    super(param);
    this.param = param;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    AxesRender.INSTANCE.render(geometricLayer, graphics);
  }

  public static void main(String[] args) {
    launch();
  }
}
