// code by jph
package ch.alpine.ascona.crv.sub;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.ren.ColorPairs;
import ch.alpine.ascony.ren.Curvature2DRender;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.sym.SymLinkImages;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophis.crv.GeodesicBSplineFunction;
import ch.alpine.sophis.crv.dub.DubinsGenerator;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.red.Times;

class GeodesicBSplineFunctionDemo extends AbstractCurveDemo {
  public GeodesicBSplineFunctionDemo() {
    Tensor dubins = Tensors.fromString(
        "{{1, 0, 0}, {1, 0, 0}, {2, 0, 2.5708}, {1, 0, 2.1}, {1.5, 0, 0}, {2.3, 0, -1.2}, {1.5, 0, 0}, {4, 0, 3.14159}, {2, 0, 3.14159}, {2, 0, 0}}");
    setControlPointsSe2(DubinsGenerator.of(Tensors.vector(0, 0, 2.1), //
        Tensor.of(dubins.stream().map(Times.operator(Tensors.vector(2, 1, 1))))));
  }

  @Override
  protected int initialCount() {
    return 3;
  }

  @Override // from RenderInterface
  public Tensor protected_render(GeometricLayer geometricLayer, Graphics2D graphics, Tensor control) {
    final int upper = control.length() - 1;
    final Scalar parameter = abstractCurveParam.ratio.multiply(RealScalar.of(upper));
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, control, null, geometricLayer, graphics);
      leversRender.renderIndexP();
    }
    // ---
    Tensor effective = control;
    ScalarTensorFunction scalarTensorFunction = //
        GeodesicBSplineFunction.of(manifoldDisplay.geodesicSpace(), abstractCurveParam.degree, effective);
    {
      Tensor selected = scalarTensorFunction.apply(parameter);
      manifoldDisplay.showPoints(ColorPairs.MARKER, RealScalar.of(1.2), Tensors.of(selected)) //
          .render(geometricLayer, graphics);
    }
    int max = upper * (1 << abstractCurveParam.refine);
    Tensor refined = Subdivide.of(0, upper, Math.max(1, max)).maps(scalarTensorFunction);
    Tensor render = manifoldDisplay.point2xy().slash(refined);
    if (manifoldDisplay.isXYeuclid())
      Curvature2DRender.of(render, false).render(geometricLayer, graphics);
    if (abstractCurveParam.refine < 5)
      manifoldDisplay.showPoints(ColorPairs.INTERMEDIATE, RealScalar.ONE, refined).render(geometricLayer, graphics);
    return refined;
  }

  @Override
  protected BufferedImage createImage() {
    final int upper = getGeodesicControlPoints().length() - 1;
    final Scalar parameter = abstractCurveParam.ratio.multiply(RealScalar.of(upper));
    return SymLinkImages.symLinkImageGBSF(abstractCurveParam.degree, upper + 1, parameter).bufferedImage();
  }

  static void main() {
    new GeodesicBSplineFunctionDemo().runStandalone();
  }
}
