// code by gjoel
package ch.alpine.ascona.ref.d1;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import ch.alpine.ascony.api.CurveVisualSet;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;

/** compare different levels of smoothing in the LaneRiesenfeldCurveSubdivision */
public class LaneRiesenfeldComparisonDemo extends ControlPointsDemo {
  private static final ColorDataIndexed COLORS = ColorDataLists._097.cyclic();
  private static final List<CurveSubdivisionSchemes> CURVE_SUBDIVISION_SCHEMES = //
      CurveSubdivisionHelper.LANE_RIESENFELD;

  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(true, ManifoldDisplays.ALL);
    }

    @FieldClip(min = "0", max = "9")
    public Integer refine = 3;
    public Boolean curv = false;
  }

  private final List<PathRender> pathRenders = new ArrayList<>();
  private final Param param;

  public LaneRiesenfeldComparisonDemo() {
    this(new Param());
  }

  public LaneRiesenfeldComparisonDemo(Param param) {
    super(param);
    this.param = param;
    setManifoldDisplay(ManifoldDisplays.Se2ClL);
    // ---
    Tensor control = Tensors.fromString("{{0, 0, 0}, {1, 0, 0}, {2, 0, 0}, {3, 1, 0}, {4, 1, 0}, {5, 0, 0}, {6, 0, 0}, {7, 0, 0}}").multiply(RealScalar.of(2));
    setControlPointsSe2(control);
    timerFrame.jToolBar.addSeparator();
    // ---
    for (int i = 0; i < CURVE_SUBDIVISION_SCHEMES.size(); ++i)
      pathRenders.add(new PathRender(COLORS.getColor(i)));
    // ---
    timerFrame.geometricComponent.setOffset(100, 600);
  }

  @Override // from RenderInterface
  public final synchronized void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Show show1 = new Show();
    show1.setPlotLabel("Curvature");
    // visualSet1.getAxisX().setLabel("length");
    // visualSet1.getAxisY().setLabel("curvature");
    // ---
    Show show2 = new Show();
    show2.setPlotLabel("Curvature d/ds");
    // visualSet2.getAxisX().setLabel("length");
    // visualSet2.getAxisY().setLabel("curvature d/ds");
    for (int i = 0; i < CURVE_SUBDIVISION_SCHEMES.size(); ++i) {
      Tensor refined = curve(geometricLayer, graphics, i);
      if (param.curv && 1 < refined.length()) {
        Tensor tensor = Tensor.of(refined.stream().map(manifoldDisplay::point2xy));
        Show show = new Show(ColorDataLists._097.cyclic().deriveWithAlpha(192));
        CurveVisualSet curveVisualSet = new CurveVisualSet(tensor);
        // VisualRow visualRow =
        curveVisualSet.addCurvature(show);
        // Tensor curvature = visualRow.points();
        // ---
        // Tensor curvatureRy = Tensor.of(Differences.of(curvature).stream().map(t -> t.Get(1).divide(t.Get(0))));
        // Tensor curvatureRx = Tensor.of(IntStream.range(1, curvature.length()).mapToObj(j -> {
        // Tensor domain = curvature.get(Tensor.ALL, 0);
        // return Mean.of(domain.extract(j - 1, j + 1));
        // }));
        // ---
        // show1.add(new ListPlot(curvature));
        // visualRow1.setLabel(CURVE_SUBDIVISION_SCHEMES.get(i).name());
        // visualRow1.setColor(COLORS.getColor(i));
        // ---
        // Showable visualRow2 = show2.add(new ListPlot(curvatureRx, curvatureRy));
        // visualRow2.setLabel(CURVE_SUBDIVISION_SCHEMES.get(i).name());
        // visualRow2.setColor(COLORS.getColor(i));
      }
    }
    // ---
    Dimension dimension = timerFrame.geometricComponent.jComponent.getSize();
    if (param.curv) {
      show1.render_autoIndent(graphics, new Rectangle(dimension.width / 2, 0, dimension.width / 2, dimension.height / 2));
      // ---
      if (!show2.isEmpty()) {
        // Tensor tensorMin = Tensor.of(show2.visualRows().stream() //
        // .map(VisualRow::points) //
        // .map(points -> points.get(Tensor.ALL, 1)) //
        // .map(CoordinateBounds::of) //
        // .map(CoordinateBoundingBox::min));
        // TODO ASCONA DEMO code below is broken
        // double min = Quantile.of(tensorMin).apply(RationalScalar.of(1, CURVE_SUBDIVISION_SCHEMES.size() - 1)).number().doubleValue();
        // Tensor tensorMax = Tensor.of(visualSet2.visualRows().stream() //
        // .map(VisualRow::points) //
        // .map(points -> points.get(Tensor.ALL, 1)) //
        // .map(CoordinateBounds::of) //
        // .map(CoordinateBoundingBox::max));
        // double max = Quantile.of(tensorMax) //
        // .apply(RationalScalar.of(CURVE_SUBDIVISION_SCHEMES.size() - 1, CurveSubdivisionHelper.LANE_RIESENFELD.size() - 1)).number().doubleValue();
        // if (min != max)
      }
      show2.render_autoIndent(graphics, new Rectangle(dimension.width / 2, dimension.height / 2, dimension.width / 2, dimension.height / 2));
    }
    RenderQuality.setDefault(graphics);
  }

  public Tensor curve(GeometricLayer geometricLayer, Graphics2D graphics, int index) {
    CurveSubdivisionSchemes scheme = CURVE_SUBDIVISION_SCHEMES.get(index);
    PathRender pathRender = pathRenders.get(index);
    // ---
    Tensor control = getGeodesicControlPoints();
    int levels = param.refine;
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
    Tensor refined = StaticHelper.refine(control, levels, scheme.of(manifoldDisplay), //
        scheme.isDual(), false, geodesicSpace);
    // ---
    Tensor render = Tensor.of(refined.stream().map(manifoldDisplay::point2xy));
    pathRender.setCurve(render, false);
    pathRender.render(geometricLayer, graphics);
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, control, null, geometricLayer, graphics);
      // leversRender.renderSequence();
      leversRender.renderIndexP();
    }
    return refined;
  }

  static void main() {
    launch();
  }
}
