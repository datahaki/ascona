// code by jph
package ch.alpine.ascona.gbc.d2;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import ch.alpine.ascony.api.LogWeightings;
import ch.alpine.ascony.arp.ArrayFunction;
import ch.alpine.ascony.arp.ArrayPlotRecord;
import ch.alpine.ascony.arp.D2Raster;
import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.ascony.ref.AsconaParam;
import ch.alpine.ascony.ren.LeversRender;
import ch.alpine.ascony.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.fig.ArrayPlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophus.hs.Manifold;
import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.alg.Rescale;
import ch.alpine.tensor.api.TensorScalarFunction;
import ch.alpine.tensor.ext.Cache;
import ch.alpine.tensor.ext.Timing;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Ceiling;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Floor;
import ch.alpine.tensor.sca.N;
import ch.alpine.tensor.sca.Round;
import ch.alpine.tensor.sca.var.InversePowerVariogram;

public final class D2AveragingDemo extends ControlPointsDemo {
  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(true, ManifoldDisplays.d2Rasters());
      manifoldDisplays = ManifoldDisplays.S2;
    }

    public LogWeightings logWeightings = LogWeightings.LAGRAINATE;
    public Biinvariants biinvariants = Biinvariants.METRIC;
    @FieldSelectionArray({ "30", "40", "50", "75", "100", "150", "200", "250" })
    public Integer resolution = 50;
    @FieldSlider
    @FieldClip(min = "0", max = "0.2")
    public Scalar radius = RealScalar.of(0.1);
    public ColorDataGradients cdg = ColorDataGradients.PARULA;
  }

  private final Param param;

  public D2AveragingDemo() {
    this(new Param());
  }

  public D2AveragingDemo(Param param) {
    super(param);
    this.param = param;
    controlPointsRender.setMidpointIndicated(false);
    // ---
    setControlPointsSe2(Tensors.fromString("{{0, 0, 1}, {1, 0, 1}, {-1, 1, 0}, {-0.5, -1, 0}, {0.4, 1, 0}}"));
    fieldsEditor(0).addUniversalListener(this::recompute);
    // ---
    timerFrame.geometricComponent.setOffset(400, 400);
  }

  private final Cache<Tensor, ArrayPlotRecord> cache = Cache.of(this::computeImage, 1);
  private double computeTime = 0;

  protected void recompute() {
    System.out.println("clear");
    cache.clear();
  }

  private ArrayPlotRecord computeImage(Tensor tensor) {
    Tensor sequence = tensor.get(0).maps(N.DOUBLE);
    Tensor values = tensor.get(1).maps(N.DOUBLE);
    int resolution = param.resolution;
    if (2 < values.length())
      try {
        ManifoldDisplay manifoldDisplay = manifoldDisplay();
        Manifold manifold = (Manifold) manifoldDisplay.geodesicSpace();
        TensorScalarFunction tensorScalarFunction = param.logWeightings.function( //
            param.biinvariants.ofSafe(manifold), //
            InversePowerVariogram.of(2), sequence, values);
        D2Raster d2Raster = (D2Raster) manifoldDisplay;
        Timing timing = Timing.started();
        ArrayFunction<Scalar> arrayFunction = new ArrayFunction<>(t -> Round._1.apply(tensorScalarFunction.apply(t)), DoubleScalar.INDETERMINATE);
        Tensor matrix = D2Raster.of(d2Raster, resolution, arrayFunction);
        computeTime = timing.seconds();
        // ---
        Rescale rescale = new Rescale(matrix);
        Clip clip = rescale.clip();
        ColorDataGradient colorDataGradient = StaticHelper.custom(param.cdg, clip, param.radius);
        Set<Scalar> set = new HashSet<>();
        Range.of(Ceiling.intValueExact(clip.min()), Floor.intValueExact(clip.max()) + 1).stream() //
            .map(Scalar.class::cast) //
            .forEach(set::add);
        return new ArrayPlotRecord(matrix, colorDataGradient);
      } catch (Exception exception) {
        exception.printStackTrace();
      }
    return null;
  }

  @Override
  public final void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    D2Raster d2Raster = (D2Raster) manifoldDisplay;
    CoordinateBoundingBox coordinateBoundingBox = d2Raster.coordinateBoundingBox();
    Tensor sequence = getGeodesicControlPoints();
    Tensor values = getControlPointsSe2().get(Tensor.ALL, 2);
    ArrayPlotRecord arrayPlotRecord = cache.apply(Unprotect.byRef(sequence, values));
    if (Objects.nonNull(arrayPlotRecord)) {
      int height = 300;
      Show show = new Show();
      show.add(ArrayPlot.of(arrayPlotRecord.matrix(), coordinateBoundingBox, arrayPlotRecord.cdg()));
      show.render(graphics, new Rectangle(70, 50, height, height));
    }
    RenderQuality.setQuality(graphics);
    LeversRender leversRender = //
        LeversRender.of(manifoldDisplay, sequence, values, geometricLayer, graphics);
    leversRender.renderWeights(values);
    graphics.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
    graphics.setColor(Color.GRAY);
    graphics.drawString("compute: " + RealScalar.of(computeTime).maps(Round._3), 0, 30);
  }

  static void main() {
    launch();
  }
}
