// code by jph
package ch.alpine.ascona.crv.d2;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.AxesRender;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.PointsRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.d2.ParametricResample;
import ch.alpine.sophis.crv.d2.ResampleResult;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.PadRight;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.lie.rot.CirclePoints;

public class R2ParametricResampleDemo extends ControlPointsDemo {
  private static final ColorDataIndexed COLOR_DATA_INDEXED = ColorDataLists._097.strict().deriveWithAlpha(128);
  private static final PointsRender POINTS_RENDER = new PointsRender(new Color(0, 128, 128, 64), new Color(0, 128, 128, 255));

  // ---
  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(true, ManifoldDisplays.R2_ONLY);
    }

    @FieldClip(min = "0", max = "10")
    public Scalar threshold = RealScalar.of(3);
    public Scalar ds = RealScalar.of(0.3);

    public ParametricResample parametricResample() {
      return new ParametricResample(threshold, ds);
    }
  }

  private final Param param;

  public R2ParametricResampleDemo() {
    this(new Param());
  }

  public R2ParametricResampleDemo(Param param) {
    super(param);
    this.param = param;
    // ---
    int n = 20;
    setControlPointsSe2(PadRight.zeros(n, 3).apply(CirclePoints.of(n).multiply(RealScalar.of(3))));
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    AxesRender.INSTANCE.render(geometricLayer, graphics);
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor control = getGeodesicControlPoints();
    graphics.setColor(COLOR_DATA_INDEXED.getColor(0));
    graphics.setStroke(new BasicStroke(2f));
    graphics.draw(geometricLayer.toPath2D(control));
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, control, null, geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderIndexP();
    }
    // ---
    ParametricResample parametricResample = param.parametricResample();
    ResampleResult resampleResult = parametricResample.apply(control);
    for (Tensor points : resampleResult.getPoints())
      POINTS_RENDER.show(manifoldDisplay::matrixLift, manifoldDisplay.shape(), points) //
          .render(geometricLayer, graphics);
  }

  static void main() {
    launch();
  }
}
