// code by gjoel
package ch.alpine.ascona.ref.d1;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.jfree.chart.JFreeChart;

import ch.alpine.ascona.util.api.CurveVisualSet;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.ren.PathRender;
import ch.alpine.ascona.util.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.fig.ListPlot;
import ch.alpine.bridge.fig.VisualRow;
import ch.alpine.bridge.fig.VisualSet;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Differences;
import ch.alpine.tensor.img.ColorDataIndexed;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.opt.nd.CoordinateBounds;
import ch.alpine.tensor.red.Mean;

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

    @FieldInteger
    @FieldClip(min = "0", max = "9")
    public Scalar refine = RealScalar.of(3);
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
  public synchronized final void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    VisualSet visualSet1 = new VisualSet();
    visualSet1.setPlotLabel("Curvature");
    visualSet1.getAxisX().setLabel("length");
    visualSet1.getAxisY().setLabel("curvature");
    // ---
    VisualSet visualSet2 = new VisualSet();
    visualSet2.setPlotLabel("Curvature d/ds");
    visualSet2.getAxisX().setLabel("length");
    visualSet2.getAxisY().setLabel("curvature d/ds");
    for (int i = 0; i < CURVE_SUBDIVISION_SCHEMES.size(); ++i) {
      Tensor refined = curve(geometricLayer, graphics, i);
      if (param.curv && 1 < refined.length()) {
        Tensor tensor = Tensor.of(refined.stream().map(manifoldDisplay::point2xy));
        VisualSet visualSet = new VisualSet(ColorDataLists._097.cyclic().deriveWithAlpha(192));
        CurveVisualSet curveVisualSet = new CurveVisualSet(tensor);
        VisualRow visualRow = curveVisualSet.addCurvature(visualSet);
        Tensor curvature = visualRow.points();
        // ---
        Tensor curvatureRy = Tensor.of(Differences.of(curvature).stream().map(t -> t.Get(1).divide(t.Get(0))));
        Tensor curvatureRx = Tensor.of(IntStream.range(1, curvature.length()).mapToObj(j -> {
          Tensor domain = curvature.get(Tensor.ALL, 0);
          return Mean.of(domain.extract(j - 1, j + 1));
        }));
        // ---
        VisualRow visualRow1 = visualSet1.add(curvature);
        visualRow1.setLabel(CURVE_SUBDIVISION_SCHEMES.get(i).name());
        visualRow1.setColor(COLORS.getColor(i));
        // ---
        VisualRow visualRow2 = visualSet2.add(curvatureRx, curvatureRy);
        visualRow2.setLabel(CURVE_SUBDIVISION_SCHEMES.get(i).name());
        visualRow2.setColor(COLORS.getColor(i));
      }
    }
    // ---
    Dimension dimension = timerFrame.geometricComponent.jComponent.getSize();
    if (param.curv) {
      JFreeChart jFreeChart1 = ListPlot.of(visualSet1, true);
      jFreeChart1.draw(graphics, new Rectangle2D.Double(dimension.width * .5, 0, dimension.width * .5, dimension.height * .5));
      // ---
      JFreeChart jFreeChart2 = ListPlot.of(visualSet2, true);
      if (!visualSet2.visualRows().isEmpty()) {
        Tensor tensorMin = Tensor.of(visualSet2.visualRows().stream() //
            .map(VisualRow::points) //
            .map(points -> points.get(Tensor.ALL, 1)) //
            .map(CoordinateBounds::of) //
            .map(CoordinateBoundingBox::min));
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
        // jFreeChart2.getXYPlot().getRangeAxis().setRange(1.1 * min, 1.1 * max);
      }
      jFreeChart2.draw(graphics, new Rectangle2D.Double(dimension.width * .5, dimension.height * .5, dimension.width * .5, dimension.height * .5));
    }
    RenderQuality.setDefault(graphics);
  }

  public Tensor curve(GeometricLayer geometricLayer, Graphics2D graphics, int index) {
    CurveSubdivisionSchemes scheme = CURVE_SUBDIVISION_SCHEMES.get(index);
    PathRender pathRender = pathRenders.get(index);
    // ---
    Tensor control = getGeodesicControlPoints();
    int levels = param.refine.number().intValue();
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

  public static void main(String[] args) {
    launch();
  }
}
