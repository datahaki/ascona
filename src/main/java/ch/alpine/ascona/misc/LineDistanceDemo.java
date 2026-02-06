// code by jph
package ch.alpine.ascona.misc;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

import ch.alpine.ascony.arp.ArrayFunction;
import ch.alpine.ascony.arp.D2Raster;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.ImageRender;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldLabel;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.decim.LineDistance;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.math.api.TensorNorm;
import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Rescale;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarTensorFunction;
import ch.alpine.tensor.api.TensorScalarFunction;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.io.ImageFormat;
import ch.alpine.tensor.red.Times;

public class LineDistanceDemo extends ControlPointsDemo {
  private static final Stroke STROKE = //
      new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0);
  private static final Tensor GEODESIC_DOMAIN = Subdivide.of(0.0, 1.0, 11);
  private static final Tensor INITIAL = Tensors.fromString("{{-0.5, 0, 0}, {0.5, 0, 0}}").unmodifiable();

  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(false, ManifoldDisplays.R2_S2);
    }

    @FieldSelectionArray({ "20", "30", "50", "75", "100", "150", "200" })
    public Integer resolution = 50;
    @FieldLabel("color data gradient")
    public ColorDataGradients colorDataGradients = ColorDataGradients.PARULA;
  }

  private final Param param;

  public LineDistanceDemo() {
    this(new Param());
  }

  public LineDistanceDemo(Param param) {
    super(param);
    this.param = param;
    // ---
    setControlPointsSe2(INITIAL);
    // ---
    Tensor model2pixel = timerFrame.geometricComponent.getModel2Pixel();
    timerFrame.geometricComponent.setModel2Pixel(Times.of(Tensors.vector(5, 5, 1), model2pixel));
    // ---
    timerFrame.geometricComponent.setOffset(400, 400);
  }

  TensorNorm tensorNorm() {
    LineDistance lineDistance = manifoldDisplay().lineDistance();
    Tensor cp = getGeodesicControlPoints();
    return 1 < cp.length() //
        ? lineDistance.tensorNorm(cp.get(0), cp.get(1))
        : _ -> RealScalar.ZERO;
  }

  private BufferedImage bufferedImage(int resolution) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    D2Raster d2Raster = (D2Raster) manifoldDisplay;
    TensorScalarFunction tsf = tensorNorm()::norm;
    ArrayFunction<Scalar> arrayFunction = new ArrayFunction<>(tsf, DoubleScalar.INDETERMINATE);
    Tensor matrix = D2Raster.of(d2Raster, resolution, arrayFunction);
    matrix = Rescale.of(matrix);
    return ImageFormat.of(matrix.map(param.colorDataGradients));
  }

  double rad() {
    return 1;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    D2Raster hsArrayPlot = (D2Raster) manifoldDisplay;
    HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
    RenderQuality.setDefault(graphics);
    BufferedImage bufferedImage = bufferedImage(param.resolution);
    new ImageRender(bufferedImage, hsArrayPlot.coordinateBoundingBox()) //
        .render(geometricLayer, graphics);
    RenderQuality.setQuality(graphics);
    // ---
    Tensor cp = getGeodesicControlPoints();
    ScalarTensorFunction scalarTensorFunction = homogeneousSpace.curve(cp.get(0), cp.get(1));
    graphics.setStroke(STROKE);
    graphics.setColor(new Color(192, 192, 192));
    Tensor ms = Tensor.of(GEODESIC_DOMAIN.map(scalarTensorFunction).stream().map(manifoldDisplay::point2xy));
    graphics.draw(geometricLayer.toPath2D(ms));
    graphics.setStroke(new BasicStroke());
    // ---
    {
      LeversRender leversRender = LeversRender.of(manifoldDisplay, cp, null, geometricLayer, graphics);
      leversRender.renderSequence();
    }
  }

  static void main() {
    launch();
  }
}
