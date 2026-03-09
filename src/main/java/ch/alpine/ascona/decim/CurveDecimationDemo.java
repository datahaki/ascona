// code by jph
package ch.alpine.ascona.decim;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

import ch.alpine.ascona.dat.gok.GokartPosParam;
import ch.alpine.ascona.dat.gok.GokartPoseDatas;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ren.GridRender;
import ch.alpine.ascony.ren.PathRender;
import ch.alpine.ascony.ren.PointsRender;
import ch.alpine.ascony.win.ControlPointsSe2;
import ch.alpine.ascony.win.ManifoldDisplayDemo;
import ch.alpine.bridge.fig.ListLinePlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.decim.CurveDecimation;
import ch.alpine.sophis.decim.DecimationResult;
import ch.alpine.sophis.decim.LineDistances;
import ch.alpine.sophis.flt.CenterFilter;
import ch.alpine.sophis.flt.ga.GeodesicCenter;
import ch.alpine.sophis.ref.d1.LaneRiesenfeldCurveSubdivision;
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
  private static final Color COLOR_CURVE = new Color(255, 128, 128, 255);
  private static final Color COLOR_SHAPE = new Color(160, 160, 160, 160);
  private static final Color COLOR_RECON = new Color(128, 128, 128, 255);
  private static final int WIDTH = 480;
  private static final int HEIGHT = 360;
  // ---
  private final PathRender pathRenderCurve = new PathRender(COLOR_CURVE);
  private final PathRender pathRenderShape = new PathRender(COLOR_RECON, 2f);

  @ReflectionMarker
  static class Param {
    @FieldSelectionArray({ "0", "1", "2", "3", "5" })
    public Scalar width = RealScalar.of(2);
  }

  @ReflectionMarker
  static class Paran {
    @FieldSelectionArray({ "0", "1", "2", "3", "4", "5" })
    public Scalar level = RealScalar.of(2);
    @FieldSelectionArray({ "1", "2", "3" })
    public Scalar degre = RealScalar.of(1);
    public LineDistances type = LineDistances.STANDARD;
    public Boolean error = false;
  }

  private final GokartPosParam gokartPosParam;
  private final Param param;
  private final Paran paran;
  /** smoothed data set for rendering and input to decimation */
  protected Tensor control = Tensors.empty();

  public CurveDecimationDemo() {
    super(gokartPosParam = new GokartPosParam(), param = new Param(), paran = new Paran());
    fieldsEditor(0).addUniversalListener(this::updateState);
    fieldsEditor(1).addUniversalListener(this::updateState);
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
    // TODO make use of biinv mean
    TensorUnaryOperator geodesicCenter = GeodesicCenter.of(homogeneousSpace, WindowFunctions.GAUSSIAN.get());
    TensorUnaryOperator tensorUnaryOperator = new CenterFilter(geodesicCenter, param.width.number().intValue());
    ControlPointsSe2 controlPointsSe2 = gokartPosParam.getPosHz().getPoseSequence();
    control = tensorUnaryOperator.apply(controlPointsSe2.getGeodesicControlPoints(manifoldDisplay));
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    HomogeneousSpace homogeneousSpace = manifoldDisplay.homogeneousSpace();
    // render dataset
    pathRenderCurve.setCurve(control, false).render(geometricLayer, graphics);
    if (control.length() <= 1000) {
      Tensor shape = manifoldDisplay.shape().multiply(RealScalar.of(0.3));
      new PointsRender(new Color(255, 128, 128, 64), COLOR_CURVE) //
          .show(manifoldDisplay::matrixLift, shape, control) //
          .render(geometricLayer, graphics);
    }
    Scalar epsilon = Power.of(Rational.HALF, paran.level.number().intValue());
    CurveDecimation curveDecimation = CurveDecimation.of(paran.type.supply(homogeneousSpace), epsilon);
    DecimationResult decimationResult = curveDecimation.evaluate(control);
    Tensor simplified = decimationResult.result();
    // ---
    int level = getSelectedMD().equals(ManifoldDisplays.R2) ? 0 : 4;
    Tensor refined = Nest.of( //
        LaneRiesenfeldCurveSubdivision.of(homogeneousSpace, paran.degre.number().intValue())::string, //
        simplified, level);
    graphics.setColor(Color.DARK_GRAY);
    pathRenderShape.setCurve(refined, false).render(geometricLayer, graphics);
    {
      Tensor shape = manifoldDisplay.shape().multiply(RealScalar.of(0.8));
      new PointsRender(COLOR_SHAPE, Color.BLACK) //
          .show(manifoldDisplay::matrixLift, shape, simplified) //
          .render(geometricLayer, graphics);
    }
    if (paran.error) {
      Dimension dimension = getSize();
      Show show = new Show(ColorDataLists._097.cyclic().deriveWithAlpha(192));
      show.setPlotLabel("Reduction from " + control.length() + " to " + simplified.length() + " samples");
      // visualSet.getAxisX().setLabel("sample no.");
      // visualSet.getAxisY().setLabel("error");
      // visualSet.setPlotLabel("error");
      show.add(ListLinePlot.of(Range.of(0, control.length()), decimationResult.errors()));
      show.render_autoIndent(graphics, new Rectangle(dimension.width - WIDTH, 0, WIDTH, HEIGHT));
    }
  }

  static void main() {
    new CurveDecimationDemo().runStandalone();
  }
}
