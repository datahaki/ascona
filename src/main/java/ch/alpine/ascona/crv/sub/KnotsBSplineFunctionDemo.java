// code by jph
package ch.alpine.ascona.crv.sub;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.ColorPairs;
import ch.alpine.ascony.ren.Curvature2DRender;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.sym.SymLinkImage;
import ch.alpine.ascony.sym.SymLinkImages;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.GeodesicBSplineFunction;
import ch.alpine.sophis.win.KnotSpacing;
import ch.alpine.sophus.api.TensorMetric;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.alg.Last;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.itp.DeBoor;

class KnotsBSplineFunctionDemo extends AbstractCurveDemo {
  @ReflectionMarker
  static class Param {
    @FieldSlider
    @FieldClip(min = "0", max = "1")
    public Scalar exponent = RealScalar.ONE;
  }

  private final Param param;

  public KnotsBSplineFunctionDemo() {
    super(param = new Param());
    setManifoldDisplay(ManifoldDisplays.R2);
    abstractCurveParam.refine = 5;
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.metricManifolds();
  }

  @Override
  protected int initialCount() {
    return 3;
  }

  @Override // from RenderInterface
  protected Tensor protected_render(GeometricLayer geometricLayer, Graphics2D graphics, Tensor control) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    TensorMetric tensorMetric = (TensorMetric) manifoldDisplay.geodesicSpace();
    Tensor knots = KnotSpacing.centripetal(tensorMetric, param.exponent).apply(control);
    Scalar upper = Last.of(knots);
    Scalar parameter = abstractCurveParam.ratio.multiply(upper);
    // ---
    GeodesicBSplineFunction scalarTensorFunction = //
        GeodesicBSplineFunction.of(manifoldDisplay.geodesicSpace(), abstractCurveParam.degree, knots, control);
    // ---
    Tensor refined = Subdivide.of(RealScalar.ZERO, upper, Math.max(1, control.length() * (1 << abstractCurveParam.refine))).maps(scalarTensorFunction);
    manifoldDisplay.showPoints(ColorPairs.MARKER, RealScalar.of(1.2), Unprotect.byRef(scalarTensorFunction.apply(parameter))) //
        .render(geometricLayer, graphics);
    Tensor render = Tensor.of(refined.stream().map(manifoldDisplay::point2xy));
    if (manifoldDisplay.isXYeuclid())
      Curvature2DRender.of(render, false).render(geometricLayer, graphics);
    if (abstractCurveParam.refine < 5)
      manifoldDisplay.showPoints(ColorPairs.INTERMEDIATE, RealScalar.ONE, refined).render(geometricLayer, graphics);
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, control, null, geometricLayer, graphics);
      leversRender.renderIndexP();
    }
    return refined;
  }

  @Override
  protected BufferedImage createImage() {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor control = getGeodesicControlPoints();
    TensorMetric tensorMetric = (TensorMetric) manifoldDisplay.geodesicSpace();
    Tensor knots = KnotSpacing.centripetal(tensorMetric, param.exponent).apply(control);
    Scalar upper = Last.of(knots);
    Scalar parameter = abstractCurveParam.ratio.multiply(upper);
    GeodesicBSplineFunction scalarTensorFunction = //
        GeodesicBSplineFunction.of(manifoldDisplay.geodesicSpace(), abstractCurveParam.degree, knots, control);
    DeBoor deBoor = scalarTensorFunction.deBoor(parameter);
    SymLinkImage symLinkImage = SymLinkImages.deboor(deBoor.knots(), deBoor.degree() + 1, parameter);
    return symLinkImage.bufferedImage();
  }

  static void main() {
    new KnotsBSplineFunctionDemo().runStandalone();
  }
}
