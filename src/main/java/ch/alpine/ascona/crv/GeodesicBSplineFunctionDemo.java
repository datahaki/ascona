// code by jph
package ch.alpine.ascona.crv;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

import ch.alpine.ascona.util.api.BufferedImageSupplier;
import ch.alpine.ascona.util.api.ControlPointsStatic;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.Curvature2DRender;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.sym.SymLinkImages;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.sophus.crv.GeodesicBSplineFunction;
import ch.alpine.sophus.crv.dub.DubinsGenerator;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.red.Times;

public class GeodesicBSplineFunctionDemo extends AbstractCurveDemo implements BufferedImageSupplier {
  private BufferedImage bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
  private final AbstractCurveParam abstractCurveParam;

  public GeodesicBSplineFunctionDemo() {
    this(new AbstractCurveParam(ManifoldDisplays.ALL));
  }

  public GeodesicBSplineFunctionDemo(AbstractCurveParam abstractCurveParam) {
    super(abstractCurveParam);
    this.abstractCurveParam = abstractCurveParam;
    addButtonDubins();
    // ---
    Tensor dubins = Tensors.fromString(
        "{{1, 0, 0}, {1, 0, 0}, {2, 0, 2.5708}, {1, 0, 2.1}, {1.5, 0, 0}, {2.3, 0, -1.2}, {1.5, 0, 0}, {4, 0, 3.14159}, {2, 0, 3.14159}, {2, 0, 0}}");
    setControlPointsSe2(DubinsGenerator.of(Tensors.vector(0, 0, 2.1), //
        Tensor.of(dubins.stream().map(Times.operator(Tensors.vector(2, 1, 1))))));
  }

  @Override // from RenderInterface
  public Tensor protected_render(GeometricLayer geometricLayer, Graphics2D graphics, int degree, int levels, Tensor control) {
    final int upper = control.length() - 1;
    final Scalar parameter = abstractCurveParam.ratio.multiply(RealScalar.of(upper));
    bufferedImage = SymLinkImages.symLinkImageGBSF(degree, upper + 1, parameter).bufferedImage();
    // ---
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    RenderQuality.setQuality(graphics);
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, control, null, geometricLayer, graphics);
      leversRender.renderIndexP();
    }
    // ---
    Tensor effective = control;
    ScalarTensorFunction scalarTensorFunction = //
        GeodesicBSplineFunction.of(manifoldDisplay.geodesicSpace(), degree, effective);
    {
      Tensor selected = scalarTensorFunction.apply(parameter);
      geometricLayer.pushMatrix(manifoldDisplay.matrixLift(selected));
      Path2D path2d = geometricLayer.toPath2D(manifoldDisplay.shape());
      graphics.setColor(Color.DARK_GRAY);
      graphics.fill(path2d);
      geometricLayer.popMatrix();
    }
    Tensor refined = Subdivide.of(0, upper, Math.max(1, upper * (1 << levels))).map(scalarTensorFunction);
    Tensor render = Tensor.of(refined.stream().map(manifoldDisplay::point2xy));
    Curvature2DRender.of(render, false).render(geometricLayer, graphics);
    if (levels < 5)
      ControlPointsStatic.gray(manifoldDisplay, refined).render(geometricLayer, graphics);
    return refined;
  }

  @Override
  public BufferedImage bufferedImage() {
    return bufferedImage;
  }

  public static void main(String[] args) {
    launch();
  }
}
