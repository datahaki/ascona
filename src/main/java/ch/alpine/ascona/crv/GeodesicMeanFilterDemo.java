// code by jph
package ch.alpine.ascona.crv;

import java.awt.Graphics2D;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.ControlPointsStatic;
import ch.alpine.ascony.ren.Curvature2DRender;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.dub.DubinsGenerator;
import ch.alpine.sophis.flt.ga.GeodesicMeanFilter;
import ch.alpine.sophis.ref.d1.BSpline4CurveSubdivision;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.red.Nest;
import ch.alpine.tensor.red.Times;

public class GeodesicMeanFilterDemo extends ControlPointsDemo {
  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(true, ManifoldDisplays.ALL);
    }

    @FieldSelectionArray({ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" })
    public Integer radius = 2;
  }

  private final Param param;

  public GeodesicMeanFilterDemo() {
    this(new Param());
  }

  public GeodesicMeanFilterDemo(Param param) {
    super(param);
    this.param = param;
    {
      Tensor tensor = Tensors.fromString("{{1, 0, 0}, {2, 0, 2.5708}, {1, 0, 2.1}, {1.5, 0, 0}, {2.3, 0, -1.2}, {1.5, 0, 0}}");
      setControlPointsSe2(DubinsGenerator.of(Tensors.vector(0, 0, 2.1), //
          Tensor.of(tensor.stream().map(Times.operator(Tensors.vector(2, 1, 1))))));
    }
  }

  @Override // from RenderInterface
  public synchronized void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor control = getGeodesicControlPoints();
    int _radius = param.radius;
    TensorUnaryOperator geodesicMeanFilter = GeodesicMeanFilter.of(manifoldDisplay.geodesicSpace(), _radius);
    Tensor refined = geodesicMeanFilter.apply(control);
    Tensor curve = Nest.of(BSpline4CurveSubdivision.split2lo(manifoldDisplay.geodesicSpace())::string, refined, 7);
    Tensor render = Tensor.of(curve.stream().map(manifoldDisplay::point2xy));
    // ---
    RenderQuality.setQuality(graphics);
    Curvature2DRender.of(render, false).render(geometricLayer, graphics);
    ControlPointsStatic.gray(manifoldDisplay, refined).render(geometricLayer, graphics);
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, control, null, geometricLayer, graphics);
      leversRender.renderIndexP();
    }
  }

  static void main() {
    launch();
  }
}
