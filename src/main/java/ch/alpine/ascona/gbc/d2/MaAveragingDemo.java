// code by jph
package ch.alpine.ascona.gbc.d2;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Objects;

import ch.alpine.ascony.arp.ArrayFunction;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.fig.ArrayPlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.Showable;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophis.dv.Sedarim;
import ch.alpine.sophis.math.DistanceMatrix;
import ch.alpine.sophus.api.TensorMetric;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.ConstantArray;
import ch.alpine.tensor.api.TensorScalarFunction;
import ch.alpine.tensor.ext.Cache;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.mat.IdentityMatrix;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.qty.Quantity;
import ch.alpine.tensor.qty.Timing;
import ch.alpine.tensor.sca.Abs;
import ch.alpine.tensor.sca.AbsSquared;
import ch.alpine.tensor.sca.N;
import ch.alpine.tensor.sca.Round;
import ch.alpine.tensor.sca.var.InversePowerVariogram;

/** Reference:
 * "Circumscribed Quadrics in Barycentric Coordinates"
 * by Marc Alexa */
public final class MaAveragingDemo extends ControlPointsDemo {
  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(true);
    }

    public Biinvariants biinvariants = Biinvariants.METRIC;
    public Boolean type = false;
    // TODO adaptive resolution
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

  @Override
  public List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.d2Rasters();
  }

  private final Cache<Tensor, Showable> cache = Cache.of(this::computeImage, 1);
  private Scalar computeTime = Quantity.of(0, "s");

  protected final void recompute() {
    System.out.println("clear");
    cache.clear();
  }

  private Showable computeImage(Tensor tensor) {
    Tensor sequence = tensor.maps(N.DOUBLE);
    int resolution = param.resolution;
    int n = sequence.length();
    if (2 < n)
      try {
        ManifoldDisplay manifoldDisplay = manifoldDisplay();
        HomogeneousSpace homogeneousSpace = manifoldDisplay.homogeneousSpace();
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
        CoordinateBoundingBox cbb = manifoldDisplay.d2Raster_coordinateBoundingBox();
        Tensor matrix = manifoldDisplay.d2Raster().of(arrayFunction, cbb, resolution);
        computeTime = timing.seconds();
        // ---
        ColorDataGradient colorDataGradient = param.cdg;
        return ArrayPlot.of(matrix, cbb, colorDataGradient);
      } catch (Exception exception) {
        System.out.println(exception);
        exception.printStackTrace();
      }
    return null;
  }

  @Override
  public final void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor sequence = getGeodesicControlPoints();
    Showable showable = cache.apply(sequence);
    if (Objects.nonNull(showable)) {
      Show show = new Show();
      show.add(showable);
      show.render(graphics, geometricLayer.toRectangle(showable.fullPlotRange().orElseThrow()));
    }
    // ---
    graphics.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
    graphics.setColor(Color.GRAY);
    graphics.drawString("compute: " + computeTime.maps(Round._3), 0, 30);
  }

  static void main() {
    new MaAveragingDemo().runStandalone();
  }
}
