// code by jph
package ch.alpine.ascona.crv;

import java.awt.Graphics2D;

import ch.alpine.ascona.util.api.ControlPointsDemo;
import ch.alpine.ascona.util.api.Curvature2DRender;
import ch.alpine.ascona.util.api.DubinsGenerator;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.win.LookAndFeels;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.ref.util.ToolbarFieldsEditor;
import ch.alpine.sophus.flt.ga.GeodesicMeanFilter;
import ch.alpine.sophus.ref.d1.BSpline4CurveSubdivision;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.red.Nest;
import ch.alpine.tensor.red.Times;

@ReflectionMarker
public class GeodesicMeanFilterDemo extends ControlPointsDemo {
  @FieldInteger
  @FieldSelectionArray({ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" })
  public Scalar radius = RealScalar.of(9);

  public GeodesicMeanFilterDemo() {
    super(true, ManifoldDisplays.ALL);
    ToolbarFieldsEditor.add(this, timerFrame.jToolBar);
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
    int _radius = radius.number().intValue();
    TensorUnaryOperator geodesicMeanFilter = GeodesicMeanFilter.of(manifoldDisplay.geodesicSpace(), _radius);
    Tensor refined = geodesicMeanFilter.apply(control);
    Tensor curve = Nest.of(BSpline4CurveSubdivision.split2lo(manifoldDisplay.geodesicSpace())::string, refined, 7);
    Tensor render = Tensor.of(curve.stream().map(manifoldDisplay::point2xy));
    // ---
    RenderQuality.setQuality(graphics);
    Curvature2DRender.of(render, false, geometricLayer, graphics);
    renderPoints(manifoldDisplay, refined, geometricLayer, graphics);
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, control, null, geometricLayer, graphics);
      leversRender.renderSequence();
      leversRender.renderIndexP();
    }
  }

  public static void main(String[] args) {
    LookAndFeels.LIGHT.tryUpdateUI();
    new GeodesicMeanFilterDemo().setVisible(1000, 600);
  }
}
