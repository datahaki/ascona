// code by ob, jph
package ch.alpine.ascona.curve;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import ch.alpine.ascona.lev.LeversRender;
import ch.alpine.ascona.util.api.Curvature2DRender;
import ch.alpine.ascona.util.api.DubinsGenerator;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.dis.Se2Display;
import ch.alpine.ascona.util.win.LookAndFeels;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.FieldPreferredWidth;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.sophus.crv.GeodesicCatmullRom;
import ch.alpine.sophus.math.win.KnotSpacing;
import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.itp.LinearBinaryAverage;
import ch.alpine.tensor.red.Times;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Clips;

@ReflectionMarker
public class GeodesicCatmullRomDemo extends AbstractCurvatureDemo {
  @FieldInteger
  @FieldPreferredWidth(100)
  @FieldSelectionArray(value = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "20" })
  public Scalar refine = RealScalar.of(5);
  @FieldSlider
  @FieldPreferredWidth(300)
  @FieldClip(min = "0", max = "1")
  public Scalar evalAt = RationalScalar.HALF;
  @FieldSlider
  @FieldPreferredWidth(200)
  @FieldClip(min = "0", max = "2")
  public Scalar exponent = RealScalar.ONE;

  public GeodesicCatmullRomDemo() {
    super(ManifoldDisplays.SE2C_SE2_R2);
    ToolbarFieldsEditor.add(this, timerFrame.jToolBar);
    addButtonDubins();
    // ---
    setGeodesicDisplay(Se2Display.INSTANCE);
    {
      Tensor dubins = Tensors.fromString("{{1, 1, 0}, {1, 2, -1}, {2, 1, 0.5}}");
      setControlPointsSe2(DubinsGenerator.of(Tensors.vector(0, 0, 0), //
          Tensor.of(dubins.stream().map(Times.operator(Tensors.vector(2, 1, 1))))));
    }
  }

  @Override // from RenderInterface
  public Tensor protected_render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    final int levels = refine.number().intValue();
    final Tensor control = getGeodesicControlPoints();
    RenderQuality.setQuality(graphics);
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, control, null, geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderIndexP();
    }
    if (4 <= control.length()) {
      GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
      TensorUnaryOperator centripetalKnotSpacing = //
          KnotSpacing.centripetal(manifoldDisplay.parametricDistance(), exponent);
      Tensor knots = centripetalKnotSpacing.apply(control);
      Scalar lo = knots.Get(1);
      Scalar hi = knots.Get(knots.length() - 2);
      hi = DoubleScalar.of(Math.nextDown(hi.number().doubleValue()));
      Clip interval = Clips.interval(lo, hi);
      Scalar parameter = (Scalar) LinearBinaryAverage.INSTANCE.split(lo, hi, evalAt);
      ScalarTensorFunction scalarTensorFunction = GeodesicCatmullRom.of(geodesicSpace, knots, control);
      Tensor refined = Subdivide.increasing(interval, Math.max(1, levels * control.length())).map(scalarTensorFunction);
      {
        Tensor selected = scalarTensorFunction.apply(parameter);
        geometricLayer.pushMatrix(manifoldDisplay.matrixLift(selected));
        Path2D path2d = geometricLayer.toPath2D(manifoldDisplay.shape());
        graphics.setColor(Color.DARK_GRAY);
        graphics.fill(path2d);
        geometricLayer.popMatrix();
      }
      Tensor render = Tensor.of(refined.stream().map(manifoldDisplay::toPoint));
      Curvature2DRender.of(render, false, geometricLayer, graphics);
      return refined;
    }
    return control;
  }

  public static void main(String[] args) {
    LookAndFeels.DEFAULT.updateUI();
    new GeodesicCatmullRomDemo().setVisible(1200, 600);
  }
}
