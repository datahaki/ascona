// code by gjoel, jph
package ch.alpine.ascona.crv.sub;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.List;

import ch.alpine.ascona.crv.CurveVisualSet;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.ColorStrokeIndexed;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;

/** compare different levels of smoothing in the LaneRiesenfeldCurveSubdivision */
class LaneRiesenfeldComparisonDemo extends ControlPointsDemo {
  private static final ColorDataIndexed COLORS = ColorDataLists._097.cyclic();
  private static final List<CurveSubdivisionSchemes> CURVE_SUBDIVISION_SCHEMES = List.of( //
      CurveSubdivisionSchemes.LR2, //
      CurveSubdivisionSchemes.LR3, //
      CurveSubdivisionSchemes.LR4, //
      CurveSubdivisionSchemes.LR5 //
  );

  @ReflectionMarker
  static class Param {
    @FieldClip(min = "0", max = "9")
    public Integer refine = 3;
  }

  private final Param param;

  public LaneRiesenfeldComparisonDemo() {
    super(param = new Param());
    setManifoldDisplay(ManifoldDisplays.ClL);
    // ---
    Tensor control = Tensors.fromString("{{0, 0, 0}, {1, 0, 0}, {2, 0, 0}, {3, 1, 0}, {4, 1, 0}, {5, 0, 0}, {6, 0, 0}, {7, 0, 0}}").multiply(RealScalar.of(2));
    setControlPointsSe2(control);
  }

  @Override
  protected Collection<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.ALL;
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.CURVYCURV;
  }

  @Override // from RenderInterface
  public synchronized void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Show show1 = new Show();
    show1.setShowLabel("Curvature");
    // ---
    Show show2 = new Show();
    show2.setShowLabel("Curvature d/ds");
    for (int i = 0; i < CURVE_SUBDIVISION_SCHEMES.size(); ++i) {
      Tensor refined = curve(geometricLayer, graphics, i);
      if (1 < refined.length()) {
        Tensor tensor = Tensor.of(refined.stream().map(manifoldDisplay::point2xy));
        CurveVisualSet curveVisualSet = new CurveVisualSet(tensor);
        curveVisualSet.addCurvature(show1);
        curveVisualSet.addCurvatureD(show2);
      }
    }
    // ---
    Dimension dimension = geometricComponent().getSize();
    int width = dimension.width * 2 / 5;
    show1.render_autoIndent(graphics, new Rectangle(dimension.width - width, 0, width, dimension.height / 2));
    show2.render_autoIndent(graphics, new Rectangle(dimension.width - width, dimension.height / 2, width, dimension.height / 2));
  }

  public Tensor curve(GeometricLayer geometricLayer, Graphics2D graphics, int index) {
    CurveSubdivisionSchemes scheme = CURVE_SUBDIVISION_SCHEMES.get(index);
    // ---
    Tensor control = getGeodesicControlPoints();
    int levels = param.refine;
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor refined = scheme.refine(manifoldDisplay, control, levels, false);
    // ---
    Tensor render = Tensor.of(refined.stream().map(manifoldDisplay::point2xy));
    ColorStrokeIndexed colorStrokeIndexed = new ColorStrokeIndexed(COLORS, new BasicStroke());
    new PathRender(colorStrokeIndexed.getColorStroke(index), render, false) //
        .render(geometricLayer, graphics);
    return refined;
  }

  static void main() {
    new LaneRiesenfeldComparisonDemo().runStandalone();
  }
}
