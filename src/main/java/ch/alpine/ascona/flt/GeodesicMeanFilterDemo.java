// code by jph
package ch.alpine.ascona.flt;

import java.awt.Graphics2D;
import java.util.List;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.ColorPair;
import ch.alpine.ascony.ren.ColorStroke;
import ch.alpine.ascony.ren.Curvature2DRender;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.crv.dub.DubinsGenerator;
import ch.alpine.sophis.flt.ga.GeodesicMeanFilter;
import ch.alpine.sophis.ref.d1.BSpline4CurveSubdivision;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.red.Nest;
import ch.alpine.tensor.red.Times;

class GeodesicMeanFilterDemo extends ControlPointsDemo {
  @ReflectionMarker
  static class Param {
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

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.ALL;
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.CURVYCURV;
  }

  @Override // from RenderInterface
  public synchronized void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor control = getGeodesicControlPoints();
    int _radius = param.radius;
    TensorUnaryOperator geodesicMeanFilter = GeodesicMeanFilter.of(manifoldDisplay.geodesicSpace(), _radius);
    Tensor refined = geodesicMeanFilter.apply(control);
    Tensor curve = Nest.of(BSpline4CurveSubdivision.split2lo(manifoldDisplay.geodesicSpace())::string, refined, 7);
    Tensor euclidXY = manifoldDisplay.point2xy().slash(curve);
    // ---
    Curvature2DRender.of(euclidXY, false).render(geometricLayer, graphics);
    new PathRender(ColorStroke.CURVE, euclidXY, false).render(geometricLayer, graphics);
    manifoldDisplay.showPoints(ColorPair.INTERMEDIATE, RealScalar.ONE, refined).render(geometricLayer, graphics);
    LeversRender leversRender = LeversRender.of(manifoldDisplay, control, null, geometricLayer, graphics);
    leversRender.renderIndexP();
  }

  static void main() {
    new GeodesicMeanFilterDemo().runStandalone();
  }
}
