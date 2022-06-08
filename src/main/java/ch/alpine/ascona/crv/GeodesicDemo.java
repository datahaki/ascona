// code by jph
package ch.alpine.ascona.crv;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascona.util.api.AbstractManifoldDisplayDemo;
import ch.alpine.ascona.util.api.Curvature2DRender;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.ren.PathRender;
import ch.alpine.ascona.util.win.LookAndFeels;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;

public class GeodesicDemo extends AbstractManifoldDisplayDemo {
  private static final Color COLOR = new Color(128, 128, 128, 128);
  private static final int SPLITS = 20;
  // ---
  private final PathRender pathRender = new PathRender(new Color(128, 128, 255), //
      new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0));

  @ReflectionMarker
  public static class Param {
    public Boolean comb = true;
    public Boolean extra = false;
  }

  private final Param param = new Param();

  public GeodesicDemo() {
    super(ManifoldDisplays.ALL);
    ToolbarFieldsEditor.add(param, timerFrame.jToolBar);
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    // AxesRender.INSTANCE.render(geometricLayer, graphics);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
    Tensor xya = timerFrame.geometricComponent.getMouseSe2CState();
    graphics.setColor(COLOR);
    Tensor q = manifoldDisplay.project(xya);
    ScalarTensorFunction scalarTensorFunction = //
        geodesicSpace.curve(manifoldDisplay.project(xya.map(Scalar::zero)), q);
    for (Tensor split : Subdivide.of(0, 1, SPLITS).map(scalarTensorFunction)) {
      geometricLayer.pushMatrix(manifoldDisplay.matrixLift(split));
      graphics.fill(geometricLayer.toPath2D(manifoldDisplay.shape()));
      geometricLayer.popMatrix();
    }
    {
      Tensor sequence = Subdivide.of(0, 1, 1).map(scalarTensorFunction);
      LeversRender leversRender = LeversRender.of(manifoldDisplay, sequence, null, geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderIndexP();
    }
    if (param.comb) {
      Tensor refined = Subdivide.of(0, 1, SPLITS * 6).map(scalarTensorFunction);
      Tensor render = Tensor.of(refined.stream().map(manifoldDisplay::toPoint));
      Curvature2DRender.of(render, false, geometricLayer, graphics);
    }
    if (param.extra) {
      {
        Tensor refined = Subdivide.of(1, 1.5, SPLITS * 3).map(scalarTensorFunction);
        Tensor render = Tensor.of(refined.stream().map(manifoldDisplay::toPoint));
        // CurveCurvatureRender.of(render, false, geometricLayer, graphics);
        pathRender.setCurve(render, false);
        pathRender.render(geometricLayer, graphics);
      }
      graphics.setColor(new Color(255, 128, 128));
      for (Tensor split : Subdivide.of(1, 1.5, SPLITS).map(scalarTensorFunction)) {
        geometricLayer.pushMatrix(manifoldDisplay.matrixLift(split));
        graphics.fill(geometricLayer.toPath2D(manifoldDisplay.shape().multiply(RealScalar.of(0.3))));
        geometricLayer.popMatrix();
      }
    }
    RenderQuality.setDefault(graphics);
  }

  public static void main(String[] args) {
    LookAndFeels.LIGHT.updateUI();
    new GeodesicDemo().setVisible(600, 600);
  }
}
