// code by jph
package ch.alpine.ascona.gbc.d2;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Objects;

import org.jfree.chart.JFreeChart;

import ch.alpine.ascona.util.api.LogWeightings;
import ch.alpine.ascona.util.arp.ArrayFunction;
import ch.alpine.ascona.util.arp.ArrayPlotImage;
import ch.alpine.ascona.util.arp.D2Raster;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ref.AsconaParam;
import ch.alpine.ascona.util.ren.ImageRender;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.win.ControlPointsDemo;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.fig.ArrayPlot;
import ch.alpine.bridge.fig.VisualImage;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.dv.Biinvariants;
import ch.alpine.sophus.hs.Manifold;
import ch.alpine.sophus.math.var.InversePowerVariogram;
import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.alg.Rescale;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorScalarFunction;
import ch.alpine.tensor.ext.Cache;
import ch.alpine.tensor.ext.Timing;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.img.LinearColorDataGradient;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Clips;
import ch.alpine.tensor.sca.N;
import ch.alpine.tensor.sca.Round;

public class D2AveragingDemo extends ControlPointsDemo {
  @ReflectionMarker
  public static class Param extends AsconaParam {
    public Param() {
      super(true, ManifoldDisplays.d2Rasters());
      manifoldDisplays = ManifoldDisplays.S2;
    }

    public LogWeightings logWeightings = LogWeightings.LAGRAINATE;
    public Biinvariants biinvariants = Biinvariants.METRIC;
    @FieldInteger
    @FieldSelectionArray({ "30", "40", "50", "75", "100", "150", "200", "250" })
    public Scalar resolution = RealScalar.of(50);
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

  private static final int CACHE_SIZE = 1;
  private final Cache<Tensor, ArrayPlotImage> cache = Cache.of(this::computeImage, CACHE_SIZE);
  private double computeTime = 0;

  protected void recompute() {
    System.out.println("clear");
    cache.clear();
  }

  private final ArrayPlotImage computeImage(Tensor tensor) {
    Tensor sequence = tensor.get(0).map(N.DOUBLE);
    Tensor values = tensor.get(1).map(N.DOUBLE);
    int resolution = param.resolution.number().intValue();
    if (2 < values.length())
      try {
        ManifoldDisplay manifoldDisplay = manifoldDisplay();
        Manifold manifold = (Manifold) manifoldDisplay.geodesicSpace();
        // return logWeighting().function(biinvariant(), variogram(), sequence, values);
        TensorScalarFunction tensorScalarFunction = //
            param.logWeightings.function(param.biinvariants.ofSafe(manifold), InversePowerVariogram.of(2), sequence, values);
        D2Raster d2Raster = (D2Raster) manifoldDisplay;
        ScalarUnaryOperator suo = s -> s; // Round.toMultipleOf(RationalScalar.of(2, 10));
        TensorScalarFunction tsf = tensorScalarFunction.andThen(suo);
        Timing timing = Timing.started();
        ArrayFunction<Scalar> arrayFunction = new ArrayFunction<>(tsf, DoubleScalar.INDETERMINATE);
        Tensor matrix = D2Raster.of(d2Raster, resolution, arrayFunction);
        computeTime = timing.seconds();
        // ---
        Rescale rescale = new Rescale(matrix);
        ColorDataGradients colorDataGradients = param.cdg;
        // ColorDataGradient = colorDataGradients;
        // Rescale rescale = new Rescale(matrix);
        // Clip clip = rescale.scalarSummaryStatistics().getClip();
        Tensor domain = Subdivide.increasing(Clips.unit(), 50);
        Tensor rgba = domain.map(colorDataGradients);
        for (int c = 10; c < domain.length() - 2; c += 10) {
          rgba.set(RealScalar.of(0.25)::multiply, c, Tensor.ALL);
          rgba.set(RealScalar.of(0.5)::multiply, c - 1, Tensor.ALL);
          rgba.set(RealScalar.of(0.5)::multiply, c + 1, Tensor.ALL);
        }
        rgba.set(s -> RealScalar.of(255), Tensor.ALL, 3);
        ColorDataGradient colorDataGradient = LinearColorDataGradient.of(rgba);
        // TODO ASCONA not efficient: rescale happens twice
        return new ArrayPlotImage(rescale.result(), rescale.scalarSummaryStatistics().getClip(), colorDataGradient);
      } catch (Exception exception) {
        System.out.println(exception);
        exception.printStackTrace();
      }
    return null;
  }

  @Override
  public final void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Dimension dimension = timerFrame.geometricComponent.jComponent.getSize();
    RenderQuality.setQuality(graphics);
    prepare();
    // ---
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    D2Raster d2Raster = (D2Raster) manifoldDisplay;
    CoordinateBoundingBox coordinateBoundingBox = d2Raster.coordinateBoundingBox();
    Tensor sequence = getGeodesicControlPoints();
    Tensor values = getControlPointsSe2().get(Tensor.ALL, 2);
    ArrayPlotImage arrayPlotRender = cache.apply(Unprotect.byRef(sequence, values));
    if (Objects.nonNull(arrayPlotRender)) {
      RenderQuality.setDefault(graphics); // default so that raster becomes visible
      new ImageRender(arrayPlotRender.bufferedImage(), coordinateBoundingBox) //
          .render(geometricLayer, graphics);
      BufferedImage legend = arrayPlotRender.legend();
      graphics.drawImage(legend, dimension.width - legend.getWidth(), 0, null);
      VisualImage visualImage = new VisualImage(arrayPlotRender.bufferedImage(), coordinateBoundingBox);
      JFreeChart jFreeChart = ArrayPlot.of(visualImage);
      jFreeChart.draw(graphics, new Rectangle(0, 50, 300, 300));
    }
    RenderQuality.setQuality(graphics);
    LeversRender leversRender = //
        LeversRender.of(manifoldDisplay, sequence, values, geometricLayer, graphics);
    leversRender.renderWeights(values);
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
