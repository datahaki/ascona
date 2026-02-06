// code by jph
package ch.alpine.ascona.ref.d1h;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import ch.alpine.ascony.api.HermiteSubdivisionParam;
import ch.alpine.ascony.api.HermiteSubdivisions;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.dis.S2Display;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.ren.PointsRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldPreferredWidth;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.ref.d1h.HermiteSubdivision;
import ch.alpine.sophis.ref.d1h.TensorIteration;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.hs.s.SnExponential;
import ch.alpine.sophus.math.Do;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.red.Times;

public class S2HermiteSubdivisionDemo extends ControlPointsDemo {
  // TODO ASCONA redundant
  private static final PointsRender POINTS_RENDER_0 = //
      new PointsRender(new Color(255, 128, 128, 64), new Color(255, 128, 128, 255));
  private static final Stroke STROKE = //
      new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0);
  private static final Tensor GEODESIC_DOMAIN = Subdivide.of(0.0, 1.0, 11);

  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(true, ManifoldDisplays.S2_ONLY);
    }

    public HermiteSubdivisions scheme = HermiteSubdivisions.HERMITE1;
    public final HermiteSubdivisionParam hsp = HermiteSubdivisionParam.GLOBAL;
    @FieldSlider
    @FieldPreferredWidth(100)
    @FieldClip(min = "0", max = "8")
    public Integer refine = 4;
    // ---
    @FieldSelectionArray({ "1/8", "1/4", "1/2", "1", "3/2", "2" })
    public Scalar beta = RealScalar.ONE;
    public Boolean cyclic = false;
    public Boolean derivatives = true;
  }

  private final Param param;

  public S2HermiteSubdivisionDemo() {
    this(new Param());
  }

  public S2HermiteSubdivisionDemo(Param param) {
    super(param);
    this.param = param;
    // ---
    Tensor model2pixel = timerFrame.geometricComponent.getModel2Pixel();
    timerFrame.geometricComponent.setModel2Pixel(Times.of(Tensors.vector(5, 5, 1), model2pixel));
    timerFrame.geometricComponent.setOffset(400, 400);
    // ---
    setControlPointsSe2(Tensors.fromString("{{-0.3, 0.0, 0}, {0.0, 0.5, 0.0}, {0.5, 0.5, 1}, {0.5, -0.4, 0}}"));
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    S2Display s2Display = (S2Display) manifoldDisplay();
    Scalar vscale = param.beta;
    Tensor control = Tensor.of(getControlPointsSe2().stream().map(xya -> {
      Tensor xy0 = xya.copy();
      xy0.set(Scalar::zero, 2);
      return Tensors.of( //
          s2Display.xya2point(xy0), //
          s2Display.createTangent(xy0, xya.Get(2)).multiply(vscale));
    }));
    POINTS_RENDER_0.show(manifoldDisplay::matrixLift, manifoldDisplay.shape(), control.get(Tensor.ALL, 0)).render(geometricLayer, graphics);
    // GeodesicSpace geodesicSpace = s2Display.geodesicSpace();
    HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
    { // render tangents as geodesic on sphere
      for (Tensor ctrl : control) {
        Tensor p = ctrl.get(0); // point
        Tensor v = ctrl.get(1); // vector
        if (param.derivatives) {
          Tensor q = new SnExponential(p).exp(v); // point on sphere
          ScalarTensorFunction scalarTensorFunction = homogeneousSpace.curve(p, q);
          graphics.setStroke(STROKE);
          Tensor ms = Tensor.of(GEODESIC_DOMAIN.map(scalarTensorFunction).stream().map(s2Display::point2xy));
          graphics.setColor(Color.LIGHT_GRAY);
          graphics.draw(geometricLayer.toPath2D(ms));
        }
        {
          graphics.setStroke(new BasicStroke(1.5f));
          graphics.setColor(Color.GRAY);
          geometricLayer.pushMatrix(s2Display.matrixLift(p));
          graphics.draw(geometricLayer.toLine2D(s2Display.tangentProjection(p).apply(v)));
          geometricLayer.popMatrix();
        }
      }
    }
    HermiteSubdivision hermiteSubdivision = param.scheme.supply(homogeneousSpace);
    if (1 < control.length()) {
      TensorIteration tensorIteration = param.cyclic //
          ? hermiteSubdivision.cyclic(RealScalar.ONE, control)
          : hermiteSubdivision.string(RealScalar.ONE, control);
      int n = param.refine;
      Tensor result = Do.of(control, tensorIteration::iterate, n);
      Tensor points = result.get(Tensor.ALL, 0);
      new PathRender(Color.BLUE).setCurve(points, param.cyclic).render(geometricLayer, graphics);
      if (param.derivatives && result.length() < 100) {
        for (Tensor pv : result) {
          Tensor p = pv.get(0);
          Tensor v = pv.get(1);
          {
            Tensor q = new SnExponential(p).exp(v); // point on sphere
            ScalarTensorFunction scalarTensorFunction = homogeneousSpace.curve(p, q);
            graphics.setStroke(STROKE);
            Tensor ms = Tensor.of(GEODESIC_DOMAIN.map(scalarTensorFunction).stream().map(s2Display::point2xy));
            graphics.setColor(Color.LIGHT_GRAY);
            graphics.draw(geometricLayer.toPath2D(ms));
          }
          // {
          // Tensor pr = S2GeodesicDisplay.tangentSpace(p).dot(v);
          // geometricLayer.pushMatrix(geodesicDisplay.matrixLift(p));
          // graphics.setStroke(new BasicStroke(1f));
          // graphics.setColor(Color.LIGHT_GRAY);
          // graphics.draw(geometricLayer.toLine2D(pr));
          // geometricLayer.popMatrix();
          // }
        }
      }
    }
  }

  static void main() {
    launch();
  }
}
