// code by jph
package ch.alpine.ascona.crv;

import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.AreaRender;
import ch.alpine.ascona.util.ren.ControlPointsStatic;
import ch.alpine.ascona.util.ren.Curvature2DRender;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.sym.SymGeodesic;
import ch.alpine.ascona.util.sym.SymLinkImage;
import ch.alpine.ascona.util.sym.SymScalar;
import ch.alpine.ascona.util.sym.SymSequence;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.crv.LagrangeInterpolation;
import ch.alpine.sophus.crv.dub.DubinsGenerator;
import ch.alpine.tensor.RationalScalar;
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
@ReflectionMarker
public class LagrangeInterpolationDemo extends AbstractCurvatureDemo {
  public static class Param extends AbstractCurvatureParam {
    public Param() {
      super(ManifoldDisplays.ALL);
    }

    @FieldInteger
    @FieldSelectionArray({ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" })
    public Scalar refine = RealScalar.of(7);
    public Scalar ratio = RationalScalar.HALF;
  }

  private final Param param;

  public LagrangeInterpolationDemo() {
    this(new Param());
  }

  public LagrangeInterpolationDemo(Param param) {
    super(param);
    this.param = param;
    addButtonDubins();
    // ---
    {
      Tensor tensor = Tensors.fromString("{{1, 0, 0}, {1, 0, 2.1}}");
      setControlPointsSe2(DubinsGenerator.of(Tensors.vector(0, 0, 2.1), //
          Tensor.of(tensor.stream().map(Times.operator(Tensors.vector(2, 1, 1))))));
    }
    // ---
    setManifoldDisplay(ManifoldDisplays.R2);
  }

  @Override // from RenderInterface
  public Tensor protected_render(GeometricLayer geometricLayer, Graphics2D graphics) {
    final Tensor sequence = getGeodesicControlPoints();
    if (Tensors.isEmpty(sequence))
      return Tensors.empty();
    final Scalar parameter = param.ratio.multiply(RealScalar.of(sequence.length()));
    if (param.graph) {
      Tensor vector = SymSequence.of(sequence.length());
      ScalarTensorFunction scalarTensorFunction = LagrangeInterpolation.of(SymGeodesic.INSTANCE, vector)::at;
      Scalar scalar = N.DOUBLE.apply(parameter);
      SymScalar symScalar = (SymScalar) scalarTensorFunction.apply(scalar);
      graphics.drawImage(new SymLinkImage(symScalar).bufferedImage(), 0, 0, null);
    }
    // ---
    RenderQuality.setQuality(graphics);
    // ---
    int levels = param.refine.number().intValue();
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Interpolation interpolation = LagrangeInterpolation.of(manifoldDisplay.geodesicSpace(), getGeodesicControlPoints());
    Tensor refined = Subdivide.of(0, sequence.length(), 1 << levels).map(interpolation::at);
    Tensor render = Tensor.of(refined.stream().map(manifoldDisplay::point2xy));
    Curvature2DRender.of(render, false).render(geometricLayer, graphics);
    new AreaRender( //
        Color.DARK_GRAY, //
        manifoldDisplay::matrixLift, //
        manifoldDisplay.shape(), //
        Unprotect.byRef(interpolation.at(parameter))) //
            .render(geometricLayer, graphics);
    if (levels < 5)
      ControlPointsStatic.gray(manifoldDisplay, refined).render(geometricLayer, graphics);
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, sequence, null, geometricLayer, graphics);
      leversRender.renderIndexP();
    }
    return refined;
  }

  public static void main(String[] args) {
    launch();
  }
}
