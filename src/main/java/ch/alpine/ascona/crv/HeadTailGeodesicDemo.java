// code by jph
package ch.alpine.ascona.crv;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascona.util.api.ControlPointsDemo;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.dis.S2Display;
import ch.alpine.ascona.util.ren.AxesRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.sca.Round;

@ReflectionMarker
public class HeadTailGeodesicDemo extends ControlPointsDemo {
  @FieldInteger
  @FieldSelectionArray(value = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "20" })
  public Scalar refine = RealScalar.of(6);

  public HeadTailGeodesicDemo() {
    super(false, ManifoldDisplays.ALL);
    // ---
    setGeodesicDisplay(S2Display.INSTANCE);
    ToolbarFieldsEditor.add(this, timerFrame.jToolBar);
    // ---
    setControlPointsSe2(Tensors.fromString("{{0, 0, 0}, {1, 0, 0}}"));
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    AxesRender.INSTANCE.render(geometricLayer, graphics);
    // ---
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor controlPoints = getGeodesicControlPoints();
    Tensor p = controlPoints.get(0);
    Tensor q = controlPoints.get(1);
    ScalarTensorFunction scalarTensorFunction = manifoldDisplay.geodesicSpace().curve(p, q);
    graphics.setStroke(new BasicStroke(1.5f));
    Tensor shape = manifoldDisplay.shape();
    Tensor domain = Subdivide.of(0, 1, refine.number().intValue());
    Tensor points = domain.map(scalarTensorFunction);
    Tensor xys = Tensor.of(points.stream().map(manifoldDisplay::toPoint));
    graphics.setColor(new Color(128, 255, 0));
    graphics.draw(geometricLayer.toPath2D(xys, false));
    try {
      Scalar pseudoDistance = manifoldDisplay.parametricDistance().distance(p, q);
      {
        graphics.setColor(Color.DARK_GRAY);
        graphics.drawString("" + pseudoDistance.map(Round._4), 10, 20);
      }
    } catch (Exception exception) {
      // ---
    }
    // ---
    graphics.setColor(Color.LIGHT_GRAY);
    for (Tensor _t : domain) {
      Tensor pq = scalarTensorFunction.apply((Scalar) _t);
      geometricLayer.pushMatrix(manifoldDisplay.matrixLift(pq));
      graphics.draw(geometricLayer.toPath2D(shape, true));
      geometricLayer.popMatrix();
    }
    graphics.setColor(Color.BLUE);
    for (Tensor _t : Subdivide.of(0, 1, 1)) {
      Tensor pq = scalarTensorFunction.apply((Scalar) _t);
      geometricLayer.pushMatrix(manifoldDisplay.matrixLift(pq));
      graphics.draw(geometricLayer.toPath2D(shape, true));
      geometricLayer.popMatrix();
    }
    graphics.setStroke(new BasicStroke());
  }

  public static void main(String[] args) {
    new HeadTailGeodesicDemo().setVisible(1000, 600);
  }
}