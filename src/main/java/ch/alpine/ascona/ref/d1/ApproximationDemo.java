// code by jph
package ch.alpine.ascona.ref.d1;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import ch.alpine.ascona.util.dat.GokartPoseData;
import ch.alpine.ascona.util.dat.GokartPoseDataV2;
import ch.alpine.ascona.util.dat.GokartPoseDatas;
import ch.alpine.ascona.util.dat.GokartPoseParam;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.GridRender;
import ch.alpine.ascona.util.ren.PathRender;
import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSelectionCallback;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.flt.CenterFilter;
import ch.alpine.sophus.flt.ga.GeodesicCenter;
import ch.alpine.sophus.ref.d1.CurveSubdivision;
import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.red.Nest;
import ch.alpine.tensor.sca.Round;
import ch.alpine.tensor.sca.win.GaussianWindow;

public class ApproximationDemo extends AbstractDemo {
  private static final Color COLOR_CURVE = new Color(255, 128, 128, 255);
  private static final Color COLOR_SHAPE = new Color(160, 160, 160, 192);
  private static final Scalar MARKER_SCALE = RealScalar.of(0.1);
  private static final GridRender GRID_RENDER = new GridRender(Subdivide.of(0, 100, 10));
  private static final CurveSubdivisionSchemes[] SCHEMES = { //
      CurveSubdivisionSchemes.BSPLINE1, //
      CurveSubdivisionSchemes.BSPLINE2, //
      CurveSubdivisionSchemes.BSPLINE3, //
      CurveSubdivisionSchemes.BSPLINE4_S2LO, //
      CurveSubdivisionSchemes.FOURPOINT, //
      CurveSubdivisionSchemes.SIXPOINT };

  private record Container(ManifoldDisplay manifoldDisplay, Tensor tracked, Tensor control, Tensor refined) {
  }

  // ---
  private final PathRender pathRenderCurve = new PathRender(COLOR_CURVE);
  private final PathRender pathRenderShape = new PathRender(COLOR_SHAPE);

  @ReflectionMarker
  public static class Param extends GokartPoseParam {
    public Param(GokartPoseData gokartPoseData) {
      super(gokartPoseData, ManifoldDisplays.SE2_R2);
    }

    @FieldInteger
    @FieldSelectionArray({ "0", "2", "4", "6", "8", "10", "12", "14" })
    public Scalar width = RealScalar.of(12);
    @FieldSelectionCallback("schemes")
    public CurveSubdivisionSchemes scheme = CurveSubdivisionSchemes.BSPLINE1;
    @FieldInteger
    @FieldSelectionArray({ "0", "1", "2", "3", "4", "5", "6" })
    public Scalar level = RealScalar.of(5);

    public List<CurveSubdivisionSchemes> schemes() {
      return List.of(SCHEMES);
    }
  }

  // ---
  private Container _container = null;
  private final Param param;

  public ApproximationDemo() {
    this(GokartPoseDataV2.RACING_DAY);
  }

  public ApproximationDemo(GokartPoseData gokartPoseData) {
    this(new Param(gokartPoseData));
  }

  public ApproximationDemo(Param param) {
    super(param);
    this.param = param;
    timerFrame.geometricComponent.addRenderInterfaceBackground(GRID_RENDER);
    timerFrame.geometricComponent.setModel2Pixel(GokartPoseDatas.HANGAR_MODEL2PIXEL);
    param.string = param.gpd().list().get(0);
    fieldsEditor(0).addUniversalListener(this::updateState);
    updateState();
  }

  private void updateState() {
    // Tensor rawdata =
    Tensor rawdata = param.getPoses();
    ManifoldDisplay manifoldDisplay = param.manifoldDisplays.manifoldDisplay();
    TensorUnaryOperator tensorUnaryOperator = GeodesicCenter.of(manifoldDisplay.geodesicSpace(), GaussianWindow.FUNCTION);
    TensorUnaryOperator centerFilter = new CenterFilter(tensorUnaryOperator, param.width.number().intValue());
    Tensor tracked = centerFilter.apply(rawdata);
    int level = param.level.number().intValue();
    int steps = 1 << level;
    System.out.println(DoubleScalar.of(steps).divide(param.gpd().getSampleRate()).map(Round._3));
    Tensor control = Tensor.of(IntStream.range(0, tracked.length() / steps) //
        .map(i -> i * steps) //
        .mapToObj(tracked::get));
    CurveSubdivision curveSubdivision = //
        param.scheme.of(manifoldDisplay);
    Tensor refined = Nest.of(curveSubdivision::string, control, level);
    _container = new Container(manifoldDisplay, tracked, control, refined);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Container container = _container;
    if (Objects.isNull(container))
      return;
    RenderQuality.setQuality(graphics);
    ManifoldDisplay manifoldDisplay = container.manifoldDisplay;
    {
      Tensor tracked = container.tracked;
      pathRenderCurve.setCurve(tracked, false).render(geometricLayer, graphics);
    }
    {
      Tensor control = container.control;
      int level = param.level.number().intValue();
      final Tensor shape = manifoldDisplay.shape().multiply(MARKER_SCALE.multiply(RealScalar.of(1 + level)));
      for (Tensor point : control) {
        geometricLayer.pushMatrix(manifoldDisplay.matrixLift(point));
        Path2D path2d = geometricLayer.toPath2D(shape);
        path2d.closePath();
        graphics.setColor(new Color(255, 128, 128, 64));
        graphics.fill(path2d);
        graphics.setColor(COLOR_CURVE);
        graphics.draw(path2d);
        geometricLayer.popMatrix();
      }
    }
    {
      Tensor refined = container.refined;
      final Tensor shape = manifoldDisplay.shape().multiply(MARKER_SCALE.multiply(RealScalar.of(0.5)));
      pathRenderShape.setCurve(refined, false).render(geometricLayer, graphics);
      for (Tensor point : refined) {
        geometricLayer.pushMatrix(manifoldDisplay.matrixLift(point));
        Path2D path2d = geometricLayer.toPath2D(shape);
        path2d.closePath();
        graphics.setColor(COLOR_SHAPE);
        graphics.fill(path2d);
        graphics.setColor(Color.BLACK);
        graphics.draw(path2d);
        geometricLayer.popMatrix();
      }
    }
    RenderQuality.setDefault(graphics);
  }

  public static void main(String[] args) {
    launch();
  }
}
