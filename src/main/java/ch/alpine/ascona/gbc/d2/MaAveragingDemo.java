// code by jph
package ch.alpine.ascona.gbc.d2;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Objects;

import ch.alpine.ascona.RandomPoints;
import ch.alpine.ascona.ref.BiinvariantsParam;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.msh.ArrayFunction;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.Showable;
import ch.alpine.bridge.fig.plt.DensityPlot;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.dv.Sedarim;
import ch.alpine.sophis.math.DistanceMatrix;
import ch.alpine.sophis.var.InversePowerVariogram;
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
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.mat.IdentityMatrix;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.qty.Quantity;
import ch.alpine.tensor.qty.Timing;
import ch.alpine.tensor.sca.Abs;
import ch.alpine.tensor.sca.AbsSquared;
import ch.alpine.tensor.sca.N;
import ch.alpine.tensor.sca.Round;

/** Reference:
 * "Circumscribed Quadrics in Barycentric Coordinates"
 * by Marc Alexa */
class MaAveragingDemo extends ControlPointsDemo {
  @ReflectionMarker
  static class Param0 {
    @FieldSelectionArray({ "3", "4", "5", "6", "8", "10" })
    public Integer numel = 6;
    @FieldFuse
    public Boolean shuffle = false;
  }

  @ReflectionMarker
  static class Param1 {
    public final BiinvariantsParam biinvariantsParam = BiinvariantsParam.okay();
    public Boolean type = false;
    @FieldSelectionArray({ "30", "40", "50", "75", "100", "150", "200", "250" })
    public Integer resolution = 50;
  }

  @ReflectionMarker
  static class Param2 {
    public ColorDataGradients cdg = ColorDataGradients.PARULA;
  }

  private final Param0 param0;
  private final Param1 param1;
  private final Param2 param2;
  private final Cache<Tensor, Tensor> cache = Cache.of(this::computeImage, 1);
  private Scalar computeTime = Quantity.of(0, "s");

  public MaAveragingDemo() {
    super(param0 = new Param0(), param1 = new Param1(), param2 = new Param2());
    geometricComponent().setRotatable(false);
    setControlPointsSe2(Tensors.fromString("{{0, 0, 1}, {1, 0, 1}, {-1, 1, 0}, {-0.5, -1, 0}, {0.4, 1, 0}}"));
    fieldsEditor(param0).addUniversalListener(this::shuffle);
    fieldsEditor(param1).addUniversalListener(this::recompute);
    addChangeListener(this::shuffle);
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.d2Rasters();
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.SCATTERED;
  }

  private void shuffle() {
    setGeodesicControlPoints(RandomPoints.scattered(manifoldDisplay(), param0.numel));
    recompute();
  }

  private void recompute() {
    cache.clear();
  }

  private Tensor computeImage(Tensor tensor) {
    Tensor sequence = tensor.maps(N.DOUBLE);
    int resolution = param1.resolution;
    int n = sequence.length();
    if (2 < n)
      try {
        ManifoldDisplay manifoldDisplay = manifoldDisplay();
        HomogeneousSpace homogeneousSpace = manifoldDisplay.homogeneousSpace();
        final Tensor dist;
        if (param1.type || !(homogeneousSpace instanceof TensorMetric)) {
          dist = ConstantArray.of(RealScalar.ONE, n, n).subtract(IdentityMatrix.of(n));
        } else {
          TensorMetric tensorMetric = (TensorMetric) homogeneousSpace;
          TensorMetric msq = (p, q) -> AbsSquared.FUNCTION.apply(tensorMetric.distance(p, q));
          dist = DistanceMatrix.of(sequence, msq);
        }
        Sedarim sedarim = param1.biinvariantsParam.ofSafe(homogeneousSpace).coordinate(InversePowerVariogram.of(2), sequence);
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
        return matrix;
      } catch (Exception exception) {
        System.out.println(exception);
        exception.printStackTrace();
      }
    return null;
  }

  @Override
  public final void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor sequence = getGeodesicControlPoints();
    Tensor tensor = cache.apply(sequence);
    if (Objects.nonNull(tensor)) {
      CoordinateBoundingBox cbb = manifoldDisplay().d2Raster_coordinateBoundingBox();
      Show show = new Show();
      Showable showable = show.add(DensityPlot.of(tensor, cbb, param2.cdg));
      show.render(graphics, geometricLayer.toRectangle(showable.fullPlotRange().orElseThrow()).orElseThrow());
    }
    // ---
    graphics.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
    graphics.setColor(Color.GRAY);
    graphics.drawString("compute: " + computeTime.maps(Round._3), 0, 12);
  }

  static void main() {
    new MaAveragingDemo().runStandalone();
  }
}
