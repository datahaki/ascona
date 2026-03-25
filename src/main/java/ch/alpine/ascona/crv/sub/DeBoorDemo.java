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
import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.tensor.Rational;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.itp.DeBoor;
import ch.alpine.tensor.itp.LinearInterpolation;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;
import ch.alpine.tensor.sca.N;

class DeBoorDemo extends AbstractCurveDemo {
  @Override
  protected int initialCount() {
    return 4;
  }

  @Override // from RenderInterface
  public Tensor protected_render(GeometricLayer geometricLayer, Graphics2D graphics, Tensor control) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    int n = control.length();
    final int upper = n - 1;
    Clip clip = Clips.interval(Rational.of(2 * upper - 2 - n + 2, 2), Rational.of(2 * upper + n - 2, 2));
    final Scalar parameter = LinearInterpolation.of(clip).apply(abstractCurveParam.ratio);
    Tensor knots = Range.of(0, 2 * upper);
    // ---
    GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
    ScalarTensorFunction scalarTensorFunction = DeBoor.of(geodesicSpace, knots, control);
    Tensor refined = Subdivide.of(upper - 1, upper, //
        Math.max(1, upper * (1 << abstractCurveParam.refine))).maps(scalarTensorFunction);
    {
      Tensor selected = scalarTensorFunction.apply(parameter);
      manifoldDisplay.showPoints(ColorPairs.INTERMEDIATE, RealScalar.ONE, Tensors.of(selected)) //
          .render(geometricLayer, graphics);
    }
    Tensor render = manifoldDisplay.point2xy().slash(refined);
    if (manifoldDisplay.isXYeuclid())
      Curvature2DRender.of(render, false).render(geometricLayer, graphics);
    if (abstractCurveParam.refine < 5)
      manifoldDisplay.showPoints(ColorPairs.INTERMEDIATE, RealScalar.ONE, refined) //
          .render(geometricLayer, graphics);
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, control, null, geometricLayer, graphics);
      leversRender.renderIndexP();
    }
    return refined;
  }

  @Override
  protected BufferedImage createImage() {
    int n = getGeodesicControlPoints().length();
    final int upper = n - 1;
    Clip clip = Clips.interval(Rational.of(2 * upper - 2 - n + 2, 2), Rational.of(2 * upper + n - 2, 2));
    final Scalar parameter = LinearInterpolation.of(clip).apply(abstractCurveParam.ratio);
    Tensor knots = Range.of(0, 2 * upper);
    return SymLinkImages.deboor(knots, n, N.DOUBLE.apply(parameter)).bufferedImage();
  }

  static void main() {
    new DeBoorDemo().runStandalone();
  }
}
