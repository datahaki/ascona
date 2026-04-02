// code by jph
package ch.alpine.ascona.gbc.d2;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ch.alpine.ascona.RandomPoints;
import ch.alpine.ascona.ref.BiinvariantsParam;
import ch.alpine.ascony.api.LogWeightings;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.msh.ArrayFunction;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.win.ControlPointType;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.Showable;
import ch.alpine.bridge.fig.plt.DensityPlot;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.var.InversePowerVariogram;
import ch.alpine.sophus.api.Manifold;
import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.alg.Rescale;
import ch.alpine.tensor.api.TensorScalarFunction;
import ch.alpine.tensor.col.ColorDataGradient;
import ch.alpine.tensor.col.ColorDataGradients;
import ch.alpine.tensor.ext.Cache;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.pdf.Distribution;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.pdf.c.NormalDistribution;
import ch.alpine.tensor.qty.Quantity;
import ch.alpine.tensor.qty.Timing;
import ch.alpine.tensor.sca.Ceiling;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Floor;
import ch.alpine.tensor.sca.N;
import ch.alpine.tensor.sca.Round;

final class D2AveragingDemo extends ControlPointsDemo {
  @ReflectionMarker
  static class Param {
    public LogWeightings logWeightings = LogWeightings.LAGRAINATE;
    public final BiinvariantsParam biinvariantsParam = BiinvariantsParam.fast();
    @FieldSelectionArray({ "30", "40", "50", "75", "100", "150", "200", "250" })
    public Integer resolution = 50;
    @FieldSlider
    @FieldClip(min = "0.001", max = "1")
    public Scalar radius = RealScalar.of(0.1);
    public ColorDataGradients cdg = ColorDataGradients.PARULA;
  }

  private final Param param;

  public D2AveragingDemo() {
    super(param = new Param());
    geometricComponent().setRotatable(false);
    setControlPointsSe2(Tensors.fromString("{{0, 0, 1}, {1, 0, 1}, {-1, 1, 0}, {-0.5, -1, 0}, {0.4, 1, 0}}"));
    fieldsEditor(param).addUniversalListener(this::recompute);
    addChangeListener(this::recompute);
  }

  @Override
  protected List<ManifoldDisplays> permitted_manifoldDisplays() {
    return ManifoldDisplays.d2Rasters();
  }

  @Override
  protected ControlPointType controlPointType() {
    return ControlPointType.SCATTERED;
  }

  private final Cache<Tensor, Showable> cache = Cache.of(this::computeImage, 1);
  private Scalar computeTime = Quantity.of(0, "s");

  protected void recompute() {
    Tensor xyv = RandomPoints.scattered(manifoldDisplay(), 4);
    setGeodesicControlPoints(xyv);
    Tensor xya = getControlPointsSe2();
    Distribution d = NormalDistribution.standard();
    xya.set(_ -> RandomVariate.of(d), Tensor.ALL, 2);
    cache.clear();
  }

  private Showable computeImage(Tensor tensor) {
    Tensor sequence = tensor.get(0).maps(N.DOUBLE);
    Tensor values = tensor.get(1).maps(N.DOUBLE);
    int resolution = param.resolution;
    if (2 < values.length())
      try {
        ManifoldDisplay manifoldDisplay = manifoldDisplay();
        Manifold manifold = manifoldDisplay.manifold();
        TensorScalarFunction tensorScalarFunction = param.logWeightings.function( //
            param.biinvariantsParam.ofSafe(manifold), //
            InversePowerVariogram.of(2), sequence, values);
        Timing timing = Timing.started();
        ArrayFunction<Scalar> arrayFunction = new ArrayFunction<>(t -> Round._1.apply(tensorScalarFunction.apply(t)), DoubleScalar.INDETERMINATE);
        CoordinateBoundingBox cbb = manifoldDisplay.d2Raster_coordinateBoundingBox();
        Tensor matrix = manifoldDisplay.d2Raster().of(arrayFunction, cbb, resolution);
        computeTime = timing.seconds();
        // ---
        Rescale rescale = new Rescale(matrix);
        Clip clip = rescale.clip();
        ColorDataGradient colorDataGradient = StaticHelper.custom(param.cdg, clip, param.radius);
        Set<Scalar> set = new HashSet<>();
        Range.of(Ceiling.intValueExact(clip.min()), Floor.intValueExact(clip.max()) + 1).stream() //
            .map(Scalar.class::cast) //
            .forEach(set::add);
        return DensityPlot.of(matrix, cbb, colorDataGradient);
      } catch (Exception exception) {
        exception.printStackTrace();
      }
    return null;
  }

  @Override
  public final void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor sequence = getGeodesicControlPoints();
    Tensor values = getControlPointsSe2().get(Tensor.ALL, 2);
    Showable showable = cache.apply(Unprotect.byRef(sequence, values));
    if (Objects.nonNull(showable)) {
      Show show = new Show();
      show.add(showable);
      CoordinateBoundingBox cbb = showable.fullPlotRange().orElseThrow();
      show.render(graphics, geometricLayer.toRectangle(cbb).orElseThrow());
    }
    LeversRender leversRender = //
        LeversRender.of(manifoldDisplay, sequence, values, geometricLayer, graphics);
    leversRender.renderWeights(values);
    graphics.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
    graphics.setColor(Color.GRAY);
    graphics.drawString("compute: " + computeTime.maps(Round._3), 0, 12);
  }

  static void main() {
    new D2AveragingDemo().runStandalone();
  }
}
