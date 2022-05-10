// code by jph
package ch.alpine.ascona.ref.d1h;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import ch.alpine.ascona.util.api.ControlPointsDemo;
import ch.alpine.ascona.util.api.HermiteSubdivisions;
import ch.alpine.ascona.util.dis.GeodesicDisplayRender;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.dis.S2Display;
import ch.alpine.ascona.util.ren.PathRender;
import ch.alpine.ascona.util.ren.PointsRender;
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
import ch.alpine.sophus.api.TensorIteration;
import ch.alpine.sophus.hs.sn.SnExponential;
import ch.alpine.sophus.math.Do;
import ch.alpine.sophus.ref.d1h.HermiteSubdivision;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.red.Times;

@ReflectionMarker
public class S2HermiteSubdivisionDemo extends ControlPointsDemo {
  // TODO ASCONA redundant
  private static final PointsRender POINTS_RENDER_0 = //
      new PointsRender(new Color(255, 128, 128, 64), new Color(255, 128, 128, 255));
  public HermiteSubdivisions scheme = HermiteSubdivisions.HERMITE1;
  @FieldSlider
  @FieldPreferredWidth(100)
  @FieldInteger
  @FieldClip(min = "0", max = "8")
  public Scalar refine = RealScalar.of(4);
  // ---
  @FieldSelectionArray(value = { "1/8", "1/4", "1/2", "1", "3/2", "2" })
  public Scalar beta = RealScalar.ONE;
  public Boolean cyclic = false;
  public Boolean derivatives = true;

  public S2HermiteSubdivisionDemo() {
    super(true, ManifoldDisplays.S2_ONLY);
    // ---
    ToolbarFieldsEditor.add(this, timerFrame.jToolBar);
    timerFrame.geometricComponent.addRenderInterfaceBackground(new GeodesicDisplayRender() {
      @Override
      public ManifoldDisplay getGeodesicDisplay() {
        return manifoldDisplay();
      }
    });
    Tensor model2pixel = timerFrame.geometricComponent.getModel2Pixel();
    timerFrame.geometricComponent.setModel2Pixel(Times.of(Tensors.vector(5, 5, 1), model2pixel));
    timerFrame.geometricComponent.setOffset(400, 400);
    // ---
    setControlPointsSe2(Tensors.fromString("{{-0.3, 0.0, 0}, {0.0, 0.5, 0.0}, {0.5, 0.5, 1}, {0.5, -0.4, 0}}"));
  }

  private static final Stroke STROKE = //
      new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0);
  private static final Tensor GEODESIC_DOMAIN = Subdivide.of(0.0, 1.0, 11);

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    S2Display s2Display = (S2Display) manifoldDisplay();
    Scalar vscale = beta;
    Tensor control = Tensor.of(getControlPointsSe2().stream().map(xya -> {
      Tensor xy0 = xya.copy();
      xy0.set(Scalar::zero, 2);
      return Tensors.of( //
          s2Display.project(xy0), //
          s2Display.createTangent(xy0, xya.Get(2)).multiply(vscale));
    }));
    POINTS_RENDER_0.show(manifoldDisplay::matrixLift, getControlPointShape(), control.get(Tensor.ALL, 0)).render(geometricLayer, graphics);
    GeodesicSpace geodesicSpace = s2Display.geodesicSpace();
    { // render tangents as geodesic on sphere
      for (Tensor ctrl : control) {
        Tensor p = ctrl.get(0); // point
        Tensor v = ctrl.get(1); // vector
        if (derivatives) {
          Tensor q = new SnExponential(p).exp(v); // point on sphere
          ScalarTensorFunction scalarTensorFunction = geodesicSpace.curve(p, q);
          graphics.setStroke(STROKE);
          Tensor ms = Tensor.of(GEODESIC_DOMAIN.map(scalarTensorFunction).stream().map(s2Display::toPoint));
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
    HermiteSubdivision hermiteSubdivision = scheme.supply( //
        s2Display.homogeneousSpace(), //
        s2Display.biinvariantMean());
    if (1 < control.length()) {
      TensorIteration tensorIteration = cyclic //
          ? hermiteSubdivision.cyclic(RealScalar.ONE, control)
          : hermiteSubdivision.string(RealScalar.ONE, control);
      int n = refine.number().intValue();
      Tensor result = Do.of(control, tensorIteration::iterate, n);
      Tensor points = result.get(Tensor.ALL, 0);
      new PathRender(Color.BLUE).setCurve(points, cyclic).render(geometricLayer, graphics);
      if (derivatives && result.length() < 100) {
        for (Tensor pv : result) {
          Tensor p = pv.get(0);
          Tensor v = pv.get(1);
          {
            Tensor q = new SnExponential(p).exp(v); // point on sphere
            ScalarTensorFunction scalarTensorFunction = geodesicSpace.curve(p, q);
            graphics.setStroke(STROKE);
            Tensor ms = Tensor.of(GEODESIC_DOMAIN.map(scalarTensorFunction).stream().map(s2Display::toPoint));
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

  public static void main(String[] args) {
    new S2HermiteSubdivisionDemo().setVisible(1000, 800);
  }
}
