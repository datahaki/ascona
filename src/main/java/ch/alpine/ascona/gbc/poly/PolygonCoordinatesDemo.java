// code by jph
package ch.alpine.ascona.gbc.poly;

import java.awt.Graphics2D;
import java.util.Objects;

import ch.alpine.ascona.util.api.ImageTiling;
import ch.alpine.ascona.util.api.PolygonCoordinates;
import ch.alpine.ascona.util.arp.ArrayFunction;
import ch.alpine.ascona.util.arp.ArrayPlotImage;
import ch.alpine.ascona.util.arp.D2Raster;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.win.ControlPointsDemo;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.dv.Biinvariants;
import ch.alpine.sophus.hs.Manifold;
import ch.alpine.sophus.hs.Sedarim;
import ch.alpine.sophus.math.var.InversePowerVariogram;
import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.ConstantArray;
import ch.alpine.tensor.alg.Rescale;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.img.ColorDataGradients;

/** transfer weights from barycentric coordinates defined by set of control points
 * in the square domain (subset of R^2) to means in non-linear spaces */
public class PolygonCoordinatesDemo extends ControlPointsDemo {
  @ReflectionMarker
  public static class Param0 extends AsconaParam {
    public Param0() {
      super(true, ManifoldDisplays.d2Rasters());
      manifoldDisplays = ManifoldDisplays.S2;
    }
  }

  @ReflectionMarker
  public static class Param1 {
    public PolygonCoordinates logWeightings = PolygonCoordinates.MEAN_VALUE;
    public Biinvariants biinvariants = Biinvariants.METRIC;
    @FieldSelectionArray({ "20", "30", "40" })
    public Integer resolution = 20;
    public ColorDataGradients cdg = ColorDataGradients.PARULA;
  }

  private final Param0 param0;
  private final Param1 param1;

  public PolygonCoordinatesDemo() {
    this(new Param0(), new Param1());
  }

  public PolygonCoordinatesDemo(Param0 param, Param1 param1) {
    super(param, param1);
    this.param0 = param;
    this.param1 = param1;
    // ---
    fieldsEditor(0).addUniversalListener(this::spun);
    fieldsEditor(1).addUniversalListener(this::recompute);
    spun();
  }

  private ArrayPlotImage arrayPlotImage;

  protected final void recompute() {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Manifold manifold = (Manifold) manifoldDisplay.geodesicSpace();
    Tensor sequence = getGeodesicControlPoints();
    Sedarim sedarim = param1.logWeightings.sedarim(param1.biinvariants.ofSafe(manifold), InversePowerVariogram.of(2), sequence);
    arrayPlotImage = manifoldDisplay.dimensions() < sequence.length() //
        ? arrayPlotImage(sequence, param1.resolution, sedarim::sunder)
        : null;
  }

  protected final ArrayPlotImage arrayPlotImage(Tensor sequence, int refinement, TensorUnaryOperator tensorUnaryOperator) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    D2Raster d2Raster = (D2Raster) manifoldDisplay;
    Tensor fallback = ConstantArray.of(DoubleScalar.INDETERMINATE, sequence.length());
    ArrayFunction<Tensor> arrayFunction = new ArrayFunction<>(tensorUnaryOperator, fallback);
    Tensor wgs = D2Raster.of(d2Raster, refinement, arrayFunction);
    Rescale rescale = new Rescale(ImageTiling.of(wgs));
    // logWeighting().equals(LogWeightings.DISTANCES)
    return ArrayPlotImage.of(rescale.result(), rescale.clip(), param1.cdg);
  }

  @Override
  public final void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    if (Objects.isNull(arrayPlotImage))
      recompute();
    if (Objects.nonNull(arrayPlotImage))
      arrayPlotImage.draw(graphics);
    // ---
    LeversRender leversRender = LeversRender.of( //
        manifoldDisplay(), getGeodesicControlPoints(), null, geometricLayer, graphics);
    leversRender.renderSurfaceP();
    leversRender.renderIndexP();
  }

  public void spun() {
    if (param0.manifoldDisplays.equals(ManifoldDisplays.R2)) {
      setControlPointsSe2(Tensors.fromString( //
          "{{-0.076, -0.851, 0.000}, {-0.300, -0.992, 0.000}, {-0.689, -0.097, 0.000}, {-0.689, -0.892, 0.000}, {-1.017, -0.953, 0.000}, {-0.991, 0.113, 0.000}, {-0.465, 0.157, 0.000}, {-0.164, -0.362, 0.000}, {0.431, -0.539, 0.000}, {-0.912, 0.669, 0.000}, {-0.644, 0.967, 0.000}, {0.509, 0.840, 0.000}, {1.051, 0.495, 0.000}, {0.950, -0.209, 0.000}, {0.747, 0.469, 0.000}, {-0.461, 0.637, 0.000}, {0.956, -0.627, 0.000}}"));
    } else //
    if (param0.manifoldDisplays.equals(ManifoldDisplays.H2)) {
      setControlPointsSe2(Tensors.fromString( //
          "{{-2.900, 2.467, 0.000}, {-0.367, 2.550, 0.000}, {-0.450, 0.400, 0.000}, {-1.533, 0.250, 0.000}, {-0.600, -0.567, 0.000}, {0.250, 2.867, 0.000}, {0.400, -0.683, 0.000}, {0.867, -1.067, 0.000}, {1.450, 2.800, 0.000}, {2.300, 2.117, 0.000}, {2.700, 0.317, 0.000}, {2.183, -0.517, 0.000}, {1.183, 0.167, 0.000}, {1.683, -1.767, 0.000}, {1.600, -2.583, 0.000}, {-0.800, -2.650, 0.000}, {-2.650, -1.900, 0.000}, {-2.917, 0.550, 0.000}}"));
    } else //
    if (param0.manifoldDisplays.equals(ManifoldDisplays.S2)) {
      setControlPointsSe2(Tensors.fromString( //
          "{{-0.933, -0.325, 0.000}, {-0.708, 0.500, 0.000}, {-0.262, 0.592, 0.000}, {-0.621, 0.746, 0.000}, {-0.375, 0.879, 0.000}, {0.079, 0.979, 0.000}, {0.700, 0.567, 0.000}, {0.096, 0.775, 0.000}, {-0.233, 0.833, 0.000}, {-0.004, 0.646, 0.000}, {0.733, 0.455, 0.000}, {0.942, 0.242, 0.000}, {0.033, 0.371, 0.000}, {-0.522, 0.372, 0.000}, {-0.808, 0.042, 0.000}, {-0.192, -0.158, 0.000}, {-0.634, -0.188, 0.000}, {0.014, -0.459, 0.000}, {-0.169, 0.260, 0.000}, {0.916, 0.142, 0.000}, {0.792, -0.465, 0.000}, {0.408, -0.200, 0.000}, {0.480, 0.054, 0.000}, {0.121, -0.008, 0.000}, {0.462, -0.800, 0.000}, {0.067, -0.712, 0.000}, {-0.321, -0.621, 0.000}, {0.233, -0.933, 0.000}, {-0.071, -0.975, 0.000}, {-0.200, -0.846, 0.000}, {-0.550, -0.737, 0.000}}"));
    }
    recompute();
  }

  public static void main(String[] args) {
    launch();
  }
}
