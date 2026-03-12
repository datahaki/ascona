// code by jph
package ch.alpine.ascona.misc;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.List;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.msh.ArrayFunction;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointTypes;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.fig.DensityPlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.Showable;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.gfx.PvmBuilder;
import ch.alpine.bridge.ref.ann.FieldLabel;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.api.LineDistance;
import ch.alpine.sophus.api.TensorDistance;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.api.TensorScalarFunction;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;

class LineDistanceDemo extends ControlPointsDemo {
  private static final Stroke STROKE = //
      new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0);
  private static final Tensor GEODESIC_DOMAIN = Subdivide.of(0.0, 1.0, 11);
  private static final Tensor INITIAL = Tensors.fromString("{{-0.5, 0, 0}, {0.5, 0, 0}}").unmodifiable();

  @ReflectionMarker
  static class Param {
    @FieldSelectionArray({ "20", "30", "50", "75", "100", "150", "200" })
    public Integer resolution = 50;
    @FieldLabel("color data gradient")
    public ColorDataGradients colorDataGradients = ColorDataGradients.PARULA;
  }

  private final Param param;

  public LineDistanceDemo() {
    super(param = new Param());
    geometricComponent().setRotatable(false);
    // ---
    setControlPointsSe2(INITIAL);
    // ---
    Tensor pvm = PvmBuilder.rhs().setOffset(400, 400).setPerPixel(100).digest();
    geometricComponent().setModel2Pixel(pvm);
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.lineDistances();
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointTypes.HEAD_TAIL;
  }

  TensorDistance tensorNorm() {
    LineDistance lineDistance = manifoldDisplay().lineDistance();
    Tensor cp = getGeodesicControlPoints();
    return 1 < cp.length() //
        ? lineDistance.distanceToLine(cp.get(0), cp.get(1))
        : _ -> RealScalar.ZERO;
  }

  private Showable arrayPlot(int resolution) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    TensorScalarFunction tsf = tensorNorm()::distance;
    ArrayFunction<Scalar> arrayFunction = new ArrayFunction<>(tsf, DoubleScalar.INDETERMINATE);
    CoordinateBoundingBox cbb = manifoldDisplay.d2Raster_coordinateBoundingBox();
    Tensor matrix = manifoldDisplay.d2Raster().of(arrayFunction, cbb, resolution);
    return DensityPlot.of(matrix, cbb, param.colorDataGradients);
  }

  double rad() {
    return 1;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    HomogeneousSpace homogeneousSpace = manifoldDisplay.homogeneousSpace();
    Show show = new Show();
    Showable showable = arrayPlot(param.resolution);
    show.add(showable);
    show.render(graphics, geometricLayer.toRectangle(showable.fullPlotRange().orElseThrow()).orElseThrow());
    // ---
    Tensor cp = getGeodesicControlPoints();
    ScalarTensorFunction scalarTensorFunction = homogeneousSpace.curve(cp.get(0), cp.get(1));
    graphics.setStroke(STROKE);
    graphics.setColor(new Color(192, 192, 192));
    Tensor ms = Tensor.of(GEODESIC_DOMAIN.maps(scalarTensorFunction).stream().map(manifoldDisplay::point2xy));
    graphics.draw(geometricLayer.toPath2D(ms));
    graphics.setStroke(new BasicStroke());
    // ---
    LeversRender leversRender = LeversRender.of(manifoldDisplay, cp, null, geometricLayer, graphics);
    leversRender.renderSequence();
  }

  static void main() {
    new LineDistanceDemo().runStandalone();
  }
}
