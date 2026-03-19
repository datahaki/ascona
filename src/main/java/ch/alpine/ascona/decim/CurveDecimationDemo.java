// code by jph
package ch.alpine.ascona.decim;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

import ch.alpine.ascona.dat.GokartPoseDatas;
import ch.alpine.ascona.ref.GokartPosParam;
import ch.alpine.ascony.dat.ControlPosSe2;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.ColorPair;
import ch.alpine.ascony.ren.ColorStroke;
import ch.alpine.ascony.ren.GridRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.win.ManifoldDisplayDemo;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.plt.ListLinePlot;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.decim.CurveDecimation;
import ch.alpine.sophis.decim.DecimationResult;
import ch.alpine.sophis.decim.LineDistances;
import ch.alpine.sophis.flt.CenterFilter;
import ch.alpine.sophis.flt.ga.GeodesicCenter;
import ch.alpine.sophis.ref.d1.LaneRiesenfeldCurveSubdivision;
import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.Rational;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.img.ColorDataLists;
import ch.alpine.tensor.red.Nest;
import ch.alpine.tensor.sca.pow.Power;
import ch.alpine.tensor.sca.win.WindowFunctions;

/** demonstrates Ramer Douglas Peucker on gokart data */
class CurveDecimationDemo extends ManifoldDisplayDemo {
  private static final int WIDTH = 480;
  private static final int HEIGHT = 360;

  @ReflectionMarker
  static class Param { // for data pre-processing
    public WindowFunctions windowFunctions = WindowFunctions.GAUSSIAN;
    @FieldSelectionArray({ "0", "1", "2", "3", "5" })
    public Integer width = 2;

    public TensorUnaryOperator filter(GeodesicSpace geodesicSpace) {
      // the use of GeodesicCenter gives the option to a greater filter radius
      return new CenterFilter(GeodesicCenter.of(geodesicSpace, windowFunctions.get()), width);
    }
  }

  @ReflectionMarker
  static class Paran { // for decimation and display
    @FieldSelectionArray({ "0", "1", "2", "3", "4", "5" })
    public Integer level = 2;
    public LineDistances type = LineDistances.STANDARD;
    public Boolean error = true;
  }

  private final GokartPosParam gokartPosParam;
  private final Param param;
  private final Paran paran;
  /** smoothed data set for rendering and input to decimation */
  protected Tensor control = Tensors.empty();

  public CurveDecimationDemo() {
    super(gokartPosParam = new GokartPosParam(), param = new Param(), paran = new Paran());
    fieldsEditor(gokartPosParam).addUniversalListener(this::updateState);
    fieldsEditor(param).addUniversalListener(this::updateState);
    addChangeListener(this::updateState);
    updateState();
    // ---
    geometricComponent().addRenderInterfaceBackground(new GridRender(this::getSize));
    geometricComponent().setModel2Pixel(GokartPoseDatas.HANGAR_MODEL2PIXEL);
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.SE2_R2;
  }

  protected void updateState() {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    HomogeneousSpace homogeneousSpace = manifoldDisplay.homogeneousSpace();
    ControlPosSe2 controlPointsSe2 = gokartPosParam.getPosHz().controlPosSe2();
    control = param.filter(homogeneousSpace).apply(controlPointsSe2.getGeodesicControlPoints(manifoldDisplay));
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    HomogeneousSpace homogeneousSpace = manifoldDisplay.homogeneousSpace();
    // render dataset
    new PathRender(ColorStroke.CURVE, control, false).render(geometricLayer, graphics);
    if (control.length() <= 1000)
      manifoldDisplay.showPoints(ColorPair.DEC, RealScalar.of(0.3), control) //
          .render(geometricLayer, graphics);
    Scalar epsilon = Power.of(Rational.HALF, paran.level);
    CurveDecimation curveDecimation = CurveDecimation.of(paran.type.supply(homogeneousSpace), epsilon);
    DecimationResult decimationResult = curveDecimation.evaluate(control);
    Tensor simplified = decimationResult.result();
    // ---
    int level = getSelectedMD().equals(ManifoldDisplays.R2) ? 0 : 4;
    Tensor refined = Nest.of( //
        LaneRiesenfeldCurveSubdivision.of(homogeneousSpace, 1)::string, //
        simplified, level);
    graphics.setColor(Color.DARK_GRAY);
    new PathRender(ColorStroke.SECONDARY_CURVE, refined, false).render(geometricLayer, graphics);
    manifoldDisplay.showPoints(ColorPair.DED, RealScalar.of(0.8), simplified) //
        .render(geometricLayer, graphics);
    if (paran.error) {
      Dimension dimension = getSize();
      Show show = new Show(ColorDataLists._097.cyclic().deriveWithAlpha(192));
      show.setPlotLabel("Reduction from " + control.length() + " to " + simplified.length() + " samples");
      show.add(ListLinePlot.of(Range.of(0, control.length()), decimationResult.errors()));
      show.render_autoIndent(graphics, new Rectangle(dimension.width - WIDTH, 0, WIDTH, HEIGHT));
    }
  }

  static void main() {
    new CurveDecimationDemo().runStandalone();
  }
}
