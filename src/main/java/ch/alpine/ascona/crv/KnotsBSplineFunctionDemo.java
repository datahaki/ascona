// code by jph
package ch.alpine.ascona.crv;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import ch.alpine.ascony.api.BufferedImageSupplier;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.AreaRender;
import ch.alpine.ascony.ren.ControlPointsStatic;
import ch.alpine.ascony.ren.Curvature2DRender;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.sym.SymLinkImage;
import ch.alpine.ascony.sym.SymLinkImages;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.GeodesicBSplineFunction;
import ch.alpine.sophis.crv.dub.DubinsGenerator;
import ch.alpine.sophis.math.win.KnotSpacing;
import ch.alpine.sophus.math.api.TensorMetric;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.alg.Last;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.itp.DeBoor;
import ch.alpine.tensor.red.Times;

public class KnotsBSplineFunctionDemo extends AbstractCurveDemo implements BufferedImageSupplier {
  @ReflectionMarker
  public static class Param extends AbstractCurveParam {
    public Param() {
      super(ManifoldDisplays.metricManifolds());
    }

    @FieldSlider
    @FieldClip(min = "0", max = "1")
    public Scalar exponent = RealScalar.ONE;
  }

  private BufferedImage bufferedImage;
  private final Param param;

  public KnotsBSplineFunctionDemo() {
    this(new Param());
  }

  public KnotsBSplineFunctionDemo(Param param) {
    super(param);
    this.param = param;
    // ---
    setManifoldDisplay(ManifoldDisplays.R2);
    param.refine = 5;
    // ---
    Tensor dubins = Tensors.fromString(
        "{{1, 0, 0}, {1, 0, 0}, {2, 0, 2.5708}, {1, 0, 2.1}, {1.5, 0, 0}, {2.3, 0, -1.2}, {1.5, 0, 0}, {4, 0, 3.14159}, {2, 0, 3.14159}, {2, 0, 0}}");
    setControlPointsSe2(DubinsGenerator.of(Tensors.vector(0, 0, 2.1), //
        Tensor.of(dubins.stream().map(Times.operator(Tensors.vector(2, 1, 1))))));
  }

  @Override // from RenderInterface
  protected Tensor protected_render(GeometricLayer geometricLayer, Graphics2D graphics, int degree, int levels, Tensor control) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    TensorMetric tensorMetric = (TensorMetric) manifoldDisplay.geodesicSpace();
    Tensor knots = KnotSpacing.centripetal(tensorMetric, param.exponent).apply(control);
    Scalar upper = Last.of(knots);
    Scalar parameter = param.ratio.multiply(upper);
    // ---
    GeodesicBSplineFunction scalarTensorFunction = //
        GeodesicBSplineFunction.of(manifoldDisplay.geodesicSpace(), degree, knots, control);
    {
      DeBoor deBoor = scalarTensorFunction.deBoor(parameter);
      SymLinkImage symLinkImage = SymLinkImages.deboor(deBoor.knots(), deBoor.degree() + 1, parameter);
      bufferedImage = symLinkImage.bufferedImage();
    }
    // ---
    RenderQuality.setQuality(graphics);
    Tensor refined = Subdivide.of(RealScalar.ZERO, upper, Math.max(1, control.length() * (1 << levels))).maps(scalarTensorFunction);
    new AreaRender( //
        Color.DARK_GRAY, //
        manifoldDisplay::matrixLift, //
        manifoldDisplay.shape(), //
        Unprotect.byRef(scalarTensorFunction.apply(parameter))) //
            .render(geometricLayer, graphics);
    Tensor render = Tensor.of(refined.stream().map(manifoldDisplay::point2xy));
    Curvature2DRender.of(render, false).render(geometricLayer, graphics);
    if (levels < 5)
      ControlPointsStatic.gray(manifoldDisplay, refined).render(geometricLayer, graphics);
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, control, null, geometricLayer, graphics);
      leversRender.renderIndexP();
    }
    return refined;
  }

  @Override // from BufferedImageSupplier
  public BufferedImage bufferedImage() {
    return bufferedImage;
  }

  static void main() {
    new KnotsBSplineFunctionDemo().runStandalone();
  }
}
