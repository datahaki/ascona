// code by jph
package ch.alpine.ascona.euclid;

import java.awt.BasicStroke;
import java.awt.Graphics2D;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.ren.ColorPair;
import ch.alpine.ascony.ren.GridRender;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.EuclideanPlaneDemo;
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

class R2ParametricResampleDemo extends EuclideanPlaneDemo {
  private static final ColorDataIndexed COLOR_DATA_INDEXED = ColorDataLists._097.strict().deriveWithAlpha(128);

  @ReflectionMarker
  static class Param {
    @FieldClip(min = "0", max = "10")
    public Scalar threshold = RealScalar.of(3);
    public Scalar ds = RealScalar.of(0.3);

    public ParametricResample parametricResample() {
      return new ParametricResample(threshold, ds);
    }
  }

  private final Param param;

  public R2ParametricResampleDemo() {
    super(param = new Param());
    // ---
    int n = 20;
    setControlPointsSe2(PadRight.zeros(n, 3).apply(CirclePoints.of(n).multiply(RealScalar.of(3))));
    geometricComponent().addRenderInterfaceBackground(new GridRender(this::getSize));
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.CURVYCURV;
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
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
      manifoldDisplay.showPoints(ColorPair.R2P, RealScalar.ONE, points) //
          .render(geometricLayer, graphics);
  }

  static void main() {
    new R2ParametricResampleDemo().runStandalone();
  }
}
