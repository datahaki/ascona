// code by jph
package ch.alpine.ascona.crv.sub;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Collection;

import ch.alpine.ascona.crv.CurvatureParam;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.ColorPairs;
import ch.alpine.ascony.ren.ColorStroke;
import ch.alpine.ascony.ren.Curvature2DRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.sym.SymGeodesic;
import ch.alpine.ascony.sym.SymLinkImage;
import ch.alpine.ascony.sym.SymScalar;
import ch.alpine.ascony.sym.SymSequence;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.LagrangeInterpolation;
import ch.alpine.sophis.crv.dub.DubinsGenerator;
import ch.alpine.tensor.Rational;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.itp.Interpolation;
import ch.alpine.tensor.red.Times;
import ch.alpine.tensor.sca.N;

/** LagrangeInterpolation with extrapolation */
class LagrangeInterpolationDemo extends ControlPointsDemo {
  @ReflectionMarker
  static class Param0 {
    public final CurvatureParam cp = new CurvatureParam();
    @FieldSelectionArray({ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" })
    public Integer refine = 7;
    public Scalar ratio = Rational.HALF;
  }

  @ReflectionMarker
  static class Param1 {
    public Boolean graph = true;
  }

  private final Param0 param;
  private final Param1 param1;

  public LagrangeInterpolationDemo() {
    super(param = new Param0(), param1 = new Param1());
    Tensor tensor = Tensors.fromString("{{1, 0, 0}, {1, 0, 2.1}}");
    setControlPointsSe2(DubinsGenerator.of(Tensors.vector(0, 0, 2.1), //
        Tensor.of(tensor.stream().map(Times.operator(Tensors.vector(2, 1, 1))))));
  }

  @Override
  protected Collection<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.ALL;
  }

  @Override
  protected final ControlPointType controlPointType() {
    return ControlPointType.CURVYCURV;
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    final Tensor sequence = getGeodesicControlPoints();
    if (Tensors.nonEmpty(sequence)) {
      final Scalar parameter = param.ratio.multiply(RealScalar.of(sequence.length()));
      if (param1.graph) {
        Tensor vector = SymSequence.of(sequence.length());
        ScalarTensorFunction scalarTensorFunction = LagrangeInterpolation.of(SymGeodesic.INSTANCE, vector)::at;
        Scalar scalar = N.DOUBLE.apply(parameter);
        SymScalar symScalar = (SymScalar) scalarTensorFunction.apply(scalar);
        graphics.drawImage(new SymLinkImage(symScalar).bufferedImage(), 0, 0, null);
      }
      // ---
      int levels = param.refine;
      ManifoldDisplay manifoldDisplay = manifoldDisplay();
      Interpolation interpolation = LagrangeInterpolation.of(manifoldDisplay.geodesicSpace(), getGeodesicControlPoints());
      Tensor refined = Subdivide.of(0, sequence.length(), 1 << levels).maps(interpolation::at);
      Tensor euclidXY = manifoldDisplay.point2xy().slash(refined);
      if (manifoldDisplay.isXYeuclid())
        Curvature2DRender.of(euclidXY, false).render(geometricLayer, graphics);
      new PathRender(ColorStroke.CURVE, euclidXY, false).render(geometricLayer, graphics);
      manifoldDisplay.showPoints(ColorPairs.MARKER, RealScalar.of(1.2), Unprotect.byRef(interpolation.at(parameter))) //
          .render(geometricLayer, graphics);
      if (levels < 5)
        manifoldDisplay.showPoints(ColorPairs.INTERMEDIATE, RealScalar.ONE, refined).render(geometricLayer, graphics);
      param.cp.spawnXY(manifoldDisplay, euclidXY, new Rectangle(0, 0, 400, 300)) //
          .render(geometricLayer, graphics);
    }
  }

  static void main() {
    new LagrangeInterpolationDemo().runStandalone();
  }
}
