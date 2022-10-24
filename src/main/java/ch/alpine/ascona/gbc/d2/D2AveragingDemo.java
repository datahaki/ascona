// code by jph
package ch.alpine.ascona.gbc.d2;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSelectionArray;
import ch.alpine.bridge.ref.ann.FieldSlider;
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
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.alg.Rescale;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.api.TensorScalarFunction;
import ch.alpine.tensor.ext.Cache;
import ch.alpine.tensor.ext.Timing;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.img.LinearColorDataGradient;
import ch.alpine.tensor.itp.LinearBinaryAverage;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.Ceiling;
import ch.alpine.tensor.sca.Clip;
import ch.alpine.tensor.sca.Floor;
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

  private final Cache<Tensor, ArrayPlotImage> cache = Cache.of(this::computeImage, 1);
  private double computeTime = 0;

  protected void recompute() {
    System.out.println("clear");
    cache.clear();
  }

  private final ArrayPlotImage computeImage(Tensor tensor) {
    Tensor sequence = tensor.get(0).map(N.DOUBLE);
    Tensor values = tensor.get(1).map(N.DOUBLE);
    int resolution = param.resolution;
    if (2 < values.length())
      try {
        ManifoldDisplay manifoldDisplay = manifoldDisplay();
        Manifold manifold = (Manifold) manifoldDisplay.geodesicSpace();
        TensorScalarFunction tensorScalarFunction = //
            param.logWeightings.function(param.biinvariants.ofSafe(manifold), InversePowerVariogram.of(2), sequence, values);
        D2Raster d2Raster = (D2Raster) manifoldDisplay;
        Timing timing = Timing.started();
        ArrayFunction<Scalar> arrayFunction = new ArrayFunction<>(t -> Round._1.apply(tensorScalarFunction.apply(t)), DoubleScalar.INDETERMINATE);
        Tensor matrix = D2Raster.of(d2Raster, resolution, arrayFunction);
        computeTime = timing.seconds();
        // ---
        Rescale rescale = new Rescale(matrix);
        Clip clip = rescale.clip();
        ColorDataGradients colorDataGradients = param.cdg;
        Tensor domain = Subdivide.increasing(clip, 50);
        Tensor rgba = Tensors.empty();
        IntBlend intBlend = new IntBlend(param.radius);
        Tensor c_blck = Tensors.vector(0, 0, 0, 255);
        for (int index = 0; index < domain.length(); ++index) {
          Scalar x = domain.Get(index);
          Tensor c_rgba = clip.rescale(x).map(colorDataGradients);
          Scalar weight = intBlend.apply(x);
          Tensor split = LinearBinaryAverage.INSTANCE.split(c_blck, c_rgba, weight);
          rgba.append(split);
        }
        ColorDataGradient colorDataGradient = LinearColorDataGradient.of(rgba);
        Set<Scalar> set = new HashSet<>();
        Range.of(Ceiling.intValueExact(clip.min()), Floor.intValueExact(clip.max()) + 1).stream() //
            .map(Scalar.class::cast) //
            .forEach(set::add);
        return new ArrayPlotImage(rescale.result(), clip, colorDataGradient, set);
      } catch (Exception exception) {
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
    ArrayPlotImage arrayPlotImage = cache.apply(Unprotect.byRef(sequence, values));
    if (Objects.nonNull(arrayPlotImage)) {
      RenderQuality.setDefault(graphics); // default so that raster becomes visible
      new ImageRender(arrayPlotImage.bufferedImage(), coordinateBoundingBox) //
          .render(geometricLayer, graphics);
      int height = 300;
      BufferedImage createImage = arrayPlotImage.legend().createImage(new Dimension(10, height - 40));
      graphics.drawImage(createImage, dimension.width - createImage.getWidth(), 0, null);
      // VisualImage visualImage = new VisualImage();
      Show show = new Show();
      show.add(ArrayPlot.of(arrayPlotImage.bufferedImage(), coordinateBoundingBox));
      // Showable jFreeChart = ArrayPlot.of(visualImage);
      // FIXME
      // jFreeChart.setBackgroundImageAlignment(RectangleAlignment.CENTER_RIGHT);
      // jFreeChart.setBackgroundImage(createImage);
      // jFreeChart.setBackgroundImageAlpha(1);
      // jFreeChart.setPadding(new RectangleInsets(0, 0, 0, createImage.getWidth()));
      show.render(graphics, new Rectangle(0, 50, 320, height));
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
