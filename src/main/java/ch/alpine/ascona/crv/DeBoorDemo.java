// code by jph
package ch.alpine.ascona.crv;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

import ch.alpine.ascony.api.BufferedImageSupplier;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.ControlPointsStatic;
import ch.alpine.ascony.ren.Curvature2DRender;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.sym.SymLinkImages;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophis.crv.GeodesicBSplineFunction;
import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.tensor.Rational;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.itp.DeBoor;

// FIXME ASCONA ALG demo does not seem correct
class DeBoorDemo extends AbstractCurveDemo implements BufferedImageSupplier {
  private BufferedImage bufferedImage;

  public DeBoorDemo() {
    super(new AbstractCurveParam());
    addButtonDubins();
    // ---
    setManifoldDisplay(ManifoldDisplays.Se2C);
  }

  @Override // from RenderInterface
  public Tensor protected_render(GeometricLayer geometricLayer, Graphics2D graphics, int degree, int levels, Tensor control) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    final int upper = control.length() - 1;
    final Scalar parameter = abstractCurveParam.ratio.multiply(RealScalar.of(upper));
    Tensor knots = Range.of(0, 2 * upper);
    bufferedImage = SymLinkImages.deboor(knots, control.length(), parameter).bufferedImage();
    // ---
    GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
    ScalarTensorFunction scalarTensorFunction = //
        DeBoor.of(geodesicSpace, knots, control);
    GeodesicBSplineFunction.of(manifoldDisplay.geodesicSpace(), degree, control);
    Scalar center = Rational.of(control.length() - 1, 2);
    Tensor refined = Subdivide.of( //
        center.subtract(Rational.HALF), //
        center.add(Rational.HALF), //
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

  @Override
  public BufferedImage bufferedImage() {
    return bufferedImage;
  }

  static void main() {
    new DeBoorDemo().runStandalone();
  }
}
