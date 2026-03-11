// code by ob, jph
package ch.alpine.ascona.crv.sub;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

import ch.alpine.ascona.crv.BaseCurvatureParam;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.Curvature2DRender;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldPreferredWidth;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.GeodesicCatmullRom;
import ch.alpine.sophis.win.KnotSpacing;
import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.sophus.api.TensorMetric;
import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.Rational;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.itp.LinearBinaryAverage;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;

class CatmullRomDemo extends PointSequenceDemo {
  @ReflectionMarker
  static class Param extends BaseCurvatureParam {
    @FieldPreferredWidth(100)
    @FieldSelectionArray({ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "20" })
    public Integer refine = 5;
    @FieldSlider
    @FieldPreferredWidth(300)
    @FieldClip(min = "0", max = "1")
    public Scalar evalAt = Rational.HALF;
    @FieldSlider
    @FieldPreferredWidth(200)
    @FieldClip(min = "0", max = "2")
    public Scalar exponent = RealScalar.ONE;
  }

  private final Param param;

  public CatmullRomDemo() {
    this(new Param());
  }

  public CatmullRomDemo(Param param) {
    super(param);
    this.param = param;
    setManifoldDisplay(ManifoldDisplays.R2);
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.metricManifolds();
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    final int levels = param.refine;
    final Tensor control = getGeodesicControlPoints();
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, control, null, geometricLayer, graphics);
      leversRender.renderIndexP();
    }
    if (4 <= control.length()) {
      GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
      TensorMetric tensorMetric = (TensorMetric) manifoldDisplay.geodesicSpace();
      TensorUnaryOperator centripetalKnotSpacing = //
          KnotSpacing.centripetal(tensorMetric, param.exponent);
      Tensor knots = centripetalKnotSpacing.apply(control);
      Scalar lo = knots.Get(1);
      Scalar hi = knots.Get(knots.length() - 2);
      hi = DoubleScalar.of(Math.nextDown(hi.number().doubleValue()));
      Clip interval = Clips.interval(lo, hi);
      Scalar parameter = (Scalar) LinearBinaryAverage.INSTANCE.split(lo, hi, param.evalAt);
      ScalarTensorFunction scalarTensorFunction = GeodesicCatmullRom.of(geodesicSpace, knots, control);
      Tensor refined = Subdivide.increasing(interval, Math.max(1, levels * control.length())).maps(scalarTensorFunction);
      {
        Tensor selected = scalarTensorFunction.apply(parameter);
        manifoldDisplay.showPoints(Color.DARK_GRAY, Color.BLACK, RealScalar.ONE, Tensors.of(selected)) //
            .render(geometricLayer, graphics);
      }
      Tensor render = Tensor.of(refined.stream().map(manifoldDisplay::point2xy));
      Curvature2DRender.of(render, false).render(geometricLayer, graphics);
      param.spawn(manifoldDisplay, refined, new Rectangle(0, 0, 400, 300)) //
          .render(geometricLayer, graphics);
    }
  }

  static void main() {
    new CatmullRomDemo().runStandalone();
  }
}
