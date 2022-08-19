// code by jph
package ch.alpine.ascona.gbc.d2;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Objects;

import ch.alpine.ascona.util.arp.ArrayFunction;
import ch.alpine.ascona.util.arp.ArrayPlotImage;
import ch.alpine.ascona.util.arp.D2Raster;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.ImageRender;
import ch.alpine.ascona.util.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.dv.Biinvariants;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.hs.Sedarim;
import ch.alpine.sophus.math.DistanceMatrix;
import ch.alpine.sophus.math.api.TensorMetric;
import ch.alpine.sophus.math.var.InversePowerVariogram;
import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.ConstantArray;
import ch.alpine.tensor.alg.Rescale;
import ch.alpine.tensor.api.TensorScalarFunction;
import ch.alpine.tensor.ext.Cache;
import ch.alpine.tensor.ext.Timing;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.mat.IdentityMatrix;
import ch.alpine.tensor.sca.Abs;
import ch.alpine.tensor.sca.AbsSquared;
import ch.alpine.tensor.sca.N;
import ch.alpine.tensor.sca.Round;

/** Reference:
 * "Circumscribed Quadrics in Barycentric Coordinates"
 * by Marc Alexa */
public class MaAveragingDemo extends ControlPointsDemo {
  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(true, ManifoldDisplays.d2Rasters());
    }

    public Biinvariants biinvariants = Biinvariants.METRIC;
    public Boolean type = false;
    @FieldSelectionArray({ "30", "40", "50", "75", "100", "150", "200", "250" })
    public Integer resolution = 40;
    public ColorDataGradients cdg = ColorDataGradients.PARULA;
  }

  private final Param param;

  public MaAveragingDemo() {
    this(new Param());
  }

  public MaAveragingDemo(Param param) {
    super(param);
    this.param = param;
    controlPointsRender.setMidpointIndicated(false);
    // ---
    setControlPointsSe2(Tensors.fromString("{{0, 0, 1}, {1, 0, 1}, {-1, 1, 0}, {-0.5, -1, 0}, {0.4, 1, 0}}"));
    // ---
    timerFrame.geometricComponent.setOffset(400, 400);
  }

  private static final int CACHE_SIZE = 1;
  private final Cache<Tensor, ArrayPlotImage> cache = Cache.of(this::computeImage, CACHE_SIZE);
  private double computeTime = 0;

  protected final void recompute() {
    System.out.println("clear");
    cache.clear();
  }

  private final ArrayPlotImage computeImage(Tensor tensor) {
    Tensor sequence = tensor.map(N.DOUBLE);
    int resolution = param.resolution;
    int n = sequence.length();
    if (2 < n)
      try {
        ManifoldDisplay manifoldDisplay = manifoldDisplay();
        D2Raster d2Raster = (D2Raster) manifoldDisplay;
        HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay().geodesicSpace();
        final Tensor dist;
        if (param.type || !(homogeneousSpace instanceof TensorMetric)) {
          dist = ConstantArray.of(RealScalar.ONE, n, n).subtract(IdentityMatrix.of(n));
        } else {
          TensorMetric tensorMetric = (TensorMetric) homogeneousSpace;
          TensorMetric msq = (p, q) -> AbsSquared.FUNCTION.apply(tensorMetric.distance(p, q));
          dist = DistanceMatrix.of(sequence, msq);
        }
        Sedarim sedarim = param.biinvariants.ofSafe(homogeneousSpace).coordinate(InversePowerVariogram.of(2), sequence);
        TensorScalarFunction tsf = p -> {
          Tensor b = sedarim.sunder(p);
          return Abs.FUNCTION.apply((Scalar) dist.dot(b).dot(b));
        };
        Timing timing = Timing.started();
        ArrayFunction<Scalar> arrayFunction = new ArrayFunction<>(tsf, DoubleScalar.INDETERMINATE);
        Tensor matrix = D2Raster.of(d2Raster, resolution, arrayFunction);
        computeTime = timing.seconds();
        // ---
        ColorDataGradient colorDataGradient = param.cdg;
        Rescale rescale = new Rescale(matrix);
        return ArrayPlotImage.of(rescale.result(), rescale.scalarSummaryStatistics().getClip(), colorDataGradient);
      } catch (Exception exception) {
        System.out.println(exception);
        exception.printStackTrace();
      }
    return null;
  }

  @Override
  public final void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    prepare();
    // ---
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor sequence = getGeodesicControlPoints();
    // Tensor values = getControlPointsSe2().get(Tensor.ALL, 2);
    ArrayPlotImage arrayPlotImage = cache.apply(sequence);
    if (Objects.nonNull(arrayPlotImage)) {
      RenderQuality.setDefault(graphics); // default so that raster becomes visible
      D2Raster d2Raster = (D2Raster) manifoldDisplay;
      new ImageRender(arrayPlotImage.bufferedImage(), d2Raster.coordinateBoundingBox()) //
          .render(geometricLayer, graphics);
    }
    RenderQuality.setQuality(graphics);
    // ---
    graphics.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
    graphics.setColor(Color.GRAY);
    graphics.drawString("compute: " + RealScalar.of(computeTime).map(Round._3), 0, 30);
  }

  void prepare() {
    // ---
  }

  public static void main(String[] args) {
    launch();
  }
}
