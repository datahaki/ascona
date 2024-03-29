// code by jph
package ch.alpine.ascona.crv;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

import ch.alpine.ascona.util.api.BufferedImageSupplier;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.ControlPointsStatic;
import ch.alpine.ascona.util.ren.Curvature2DRender;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.sym.SymLinkImages;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
import ch.alpine.sophus.crv.GeodesicBSplineFunction;
import ch.alpine.sophus.crv.dub.DubinsGenerator;
import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.itp.DeBoor;
import ch.alpine.tensor.red.Times;

// FIXME ASCONA ALG demo does not seem correct
@ReflectionMarker
public class GeodesicDeBoorDemo extends AbstractCurveDemo implements BufferedImageSupplier {
  // TODO ASCONA REDUNDANT to GeodesicBSplineFunctionDemo
  private BufferedImage bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

  public GeodesicDeBoorDemo() {
    super(new AbstractCurveParam(ManifoldDisplays.ALL));
    addButtonDubins();
    ToolbarFieldsEditor.addToComponent(this, timerFrame.jToolBar);
    // ---
    setManifoldDisplay(ManifoldDisplays.Se2C);
    // ---
    Tensor dubins = Tensors.fromString("{{1, 0, 0}, {2, 0, 2.5708}}");
    setControlPointsSe2(DubinsGenerator.of(Tensors.vector(0, 0, 0), //
        Tensor.of(dubins.stream().map(Times.operator(Tensors.vector(2, 1, 1))))));
  }

  @Override // from RenderInterface
  public Tensor protected_render(GeometricLayer geometricLayer, Graphics2D graphics, int degree, int levels, Tensor control) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    final int upper = control.length() - 1;
    final Scalar parameter = abstractCurveParam.ratio.multiply(RealScalar.of(upper));
    Tensor knots = Range.of(0, 2 * upper);
    bufferedImage = SymLinkImages.deboor(knots, control.length(), parameter).bufferedImage();
    // ---
    RenderQuality.setQuality(graphics);
    // ---
    GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
    ScalarTensorFunction scalarTensorFunction = //
        DeBoor.of(geodesicSpace, knots, control);
    GeodesicBSplineFunction.of(manifoldDisplay.geodesicSpace(), degree, control);
    Scalar center = RationalScalar.of(control.length() - 1, 2);
    Tensor refined = Subdivide.of( //
        center.subtract(RationalScalar.HALF), //
        center.add(RationalScalar.HALF), //
        Math.max(1, upper * (1 << levels))).map(scalarTensorFunction);
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

  public static void main(String[] args) {
    launch();
  }
}
