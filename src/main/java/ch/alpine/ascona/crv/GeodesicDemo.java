// code by jph
package ch.alpine.ascona.crv;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import ch.alpine.ascona.util.api.ControlPointsDemo;
import ch.alpine.ascona.util.api.Curvature2DRender;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.AreaRender;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.ren.PathRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.swing.LookAndFeels;
import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;

public class GeodesicDemo extends ControlPointsDemo {
  private static final Color COLOR = new Color(128, 128, 128, 128);
  private static final int SPLITS = 20;
  // ---
  private final PathRender pathRender = new PathRender(new Color(128, 128, 255), //
      new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0));

  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(false, ManifoldDisplays.ALL);
    }

    public Boolean comb = true;
    public Boolean extra = false;
  }

  private final Param param;

  public GeodesicDemo() {
    this(new Param());
  }

  public GeodesicDemo(Param param) {
    super(param);
    this.param = param;
    setControlPointsSe2(Tensors.fromString("{{0,0,0}, {1,0,0}}"));
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = param.manifoldDisplays.manifoldDisplay();
    GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
    Tensor points = getGeodesicControlPoints();
    Tensor p = points.get(0);
    Tensor q = points.get(1);
    ScalarTensorFunction scalarTensorFunction = geodesicSpace.curve(p, q);
    new AreaRender( //
        COLOR, //
        manifoldDisplay::matrixLift, manifoldDisplay.shape(), Subdivide.of(0, 1, SPLITS).map(scalarTensorFunction)) //
            .render(geometricLayer, graphics);
    {
      Tensor sequence = Subdivide.of(0, 1, 1).map(scalarTensorFunction);
      LeversRender leversRender = LeversRender.of(manifoldDisplay, sequence, null, geometricLayer, graphics);
      leversRender.renderIndexP();
    }
    if (param.comb) {
      Tensor refined = Subdivide.of(0, 1, SPLITS * 6).map(scalarTensorFunction);
      Tensor render = Tensor.of(refined.stream().map(manifoldDisplay::point2xy));
      Curvature2DRender.of(render, false, geometricLayer, graphics);
    }
    if (param.extra) {
      {
        Tensor refined = Subdivide.of(1, 1.5, SPLITS * 3).map(scalarTensorFunction);
        Tensor render = Tensor.of(refined.stream().map(manifoldDisplay::point2xy));
        // CurveCurvatureRender.of(render, false, geometricLayer, graphics);
        pathRender.setCurve(render, false);
        pathRender.render(geometricLayer, graphics);
      }
      new AreaRender( //
          new Color(255, 128, 128), //
          manifoldDisplay::matrixLift, manifoldDisplay.shape().multiply(RealScalar.of(0.3)), Subdivide.of(1, 1.5, SPLITS).map(scalarTensorFunction)) //
              .render(geometricLayer, graphics);
    }
  }

  public static void main(String[] args) {
    LookAndFeels.LIGHT.updateComponentTreeUI();
    Param param = new Param();
    new GeodesicDemo(param).setVisible(600, 600);
  }
}
