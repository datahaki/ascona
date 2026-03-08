// code by jph
package ch.alpine.ascona.crv;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.ControlPointsStatic;
import ch.alpine.ascony.ren.Curvature2DRender;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.sym.SymLinkImages;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.tensor.Rational;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.itp.DeBoor;
import ch.alpine.tensor.itp.LinearInterpolation;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;
import ch.alpine.tensor.sca.N;

class DeBoorDemo extends AbstractCurveDemo {
  public DeBoorDemo() {
    super(new AbstractCurveParam());
    setManifoldDisplay(ManifoldDisplays.Se2C);
  }

  @Override // from RenderInterface
  public Tensor protected_render(GeometricLayer geometricLayer, Graphics2D graphics, int degree, int levels, Tensor control) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    int n = control.length();
    final int upper = n - 1;
    Clip clip = Clips.interval(Rational.of(2 * upper - 2 - n + 2, 2), Rational.of(2 * upper + n - 2, 2));
    final Scalar parameter = LinearInterpolation.of(clip).apply(abstractCurveParam.ratio);
    Tensor knots = Range.of(0, 2 * upper);
    BufferedImage bufferedImage = SymLinkImages.deboor(knots, control.length(), N.DOUBLE.apply(parameter)).bufferedImage();
    graphics.drawImage(bufferedImage, 0, 0, null);
    // ---
    GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
    ScalarTensorFunction scalarTensorFunction = //
        DeBoor.of(geodesicSpace, knots, control);
    // GeodesicBSplineFunction.of(manifoldDisplay.geodesicSpace(), degree, control);
    Tensor refined = Subdivide.of(upper - 1, upper, //
        Math.max(1, upper * (1 << levels))).maps(scalarTensorFunction);
    {
      Tensor selected = scalarTensorFunction.apply(parameter);
      geometricLayer.pushMatrix(manifoldDisplay.matrixLift(selected));
      Path2D path2d = geometricLayer.toPath2D(manifoldDisplay.shape());
      graphics.setColor(Color.DARK_GRAY);
      graphics.fill(path2d);
      geometricLayer.popMatrix();
    }
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

  static void main() {
    new DeBoorDemo().runStandalone();
  }
}
