// code by jph
package ch.alpine.ascona.gbc.d2;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JToggleButton;

import ch.alpine.ascona.gbc.AnAveragingDemo;
import ch.alpine.ascona.util.api.LogWeightings;
import ch.alpine.ascona.util.arp.HsArrayPlot;
import ch.alpine.ascona.util.arp.HsArrayPlots;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.ArrayPlotRender;
import ch.alpine.ascona.util.ren.ImageRender;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.ascona.util.win.LookAndFeels;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.swing.SpinnerLabel;
import ch.alpine.sophus.api.TensorMetric;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.hs.MetricBiinvariant;
import ch.alpine.sophus.math.DistanceMatrix;
import ch.alpine.sophus.math.var.InversePowerVariogram;
import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.api.TensorScalarFunction;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.ext.Cache;
import ch.alpine.tensor.ext.Timing;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.sca.Abs;
import ch.alpine.tensor.sca.AbsSquared;
import ch.alpine.tensor.sca.N;
import ch.alpine.tensor.sca.Round;

/** TODO ASCONA reference marc alexa */
public class MaAveragingDemo extends AnAveragingDemo {
  private final SpinnerLabel<Scalar> spinnerCvar = new SpinnerLabel<>();
  private final SpinnerLabel<Integer> spinnerMagnif = new SpinnerLabel<>();
  private final SpinnerLabel<ColorDataGradients> spinnerColorData = SpinnerLabel.of(ColorDataGradients.class);
  private final SpinnerLabel<Integer> spinnerRes = SpinnerLabel.of(20, 30, 40, 50, 75, 100, 150, 200, 250);
  private final JToggleButton jToggleVarian = new JToggleButton("est/var");
  private final JToggleButton jToggleThresh = new JToggleButton("thresh");

  public MaAveragingDemo() {
    super(ManifoldDisplays.ARRAYS);
    {
      spinnerCvar.setList(Tensors.fromString("{0, 0.01, 0.1, 0.5, 1}").stream().map(Scalar.class::cast).collect(Collectors.toList()));
      spinnerCvar.setIndex(0);
      spinnerCvar.addToComponentReduced(timerFrame.jToolBar, new Dimension(60, 28), "error");
    }
    {
      spinnerMagnif.setList(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));
      spinnerMagnif.setValue(6);
      spinnerMagnif.addToComponentReduced(timerFrame.jToolBar, new Dimension(60, 28), "magnify");
      spinnerMagnif.addSpinnerListener(v -> recompute());
    }
    {
      spinnerColorData.setValue(ColorDataGradients.PARULA);
      spinnerColorData.addToComponentReduced(timerFrame.jToolBar, new Dimension(200, 28), "color scheme");
      spinnerColorData.addSpinnerListener(v -> recompute());
    }
    {
      // spinnerRes.setArray();
      spinnerRes.setValue(30);
      spinnerRes.addToComponentReduced(timerFrame.jToolBar, new Dimension(60, 28), "resolution");
      spinnerRes.addSpinnerListener(v -> recompute());
    }
    {
      timerFrame.jToolBar.add(jToggleVarian);
      timerFrame.jToolBar.add(jToggleThresh);
    }
    {
      JButton jButton = new JButton("round");
      jButton.addActionListener(e -> {
        Tensor tensor = getControlPointsSe2().copy();
        tensor.set(Round.FUNCTION, Tensor.ALL, 2);
        setControlPointsSe2(tensor);
      });
      timerFrame.jToolBar.add(jButton);
    }
    timerFrame.jToolBar.addSeparator();
    addSpinnerListener(v -> recompute());
    timerFrame.geometricComponent.setOffset(400, 400);
    // ---
    setControlPointsSe2(Tensors.fromString("{{0, 0, 1}, {1, 0, 1}, {-1, 1, 0}, {-0.5, -1, 0}, {0.4, 1, 0}}"));
  }

  private static final int CACHE_SIZE = 1;
  private final Cache<Tensor, BufferedImage> cache = Cache.of(this::computeImage, CACHE_SIZE);
  private double computeTime = 0;

  @Override
  protected final void recompute() {
    System.out.println("clear");
    cache.clear();
  }

  private final BufferedImage computeImage(Tensor tensor) {
    Tensor sequence = tensor.map(N.DOUBLE);
    int resolution = spinnerRes.getValue();
    if (2 < sequence.length())
      try {
        HsArrayPlot hsArrayPlot = manifoldDisplay().arrayPlot();
        HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay().geodesicSpace();
        TensorMetric metric = manifoldDisplay().biinvariantMetric();
        TensorMetric msq = (p, q) -> AbsSquared.FUNCTION.apply(metric.distance(p, q));
        // msq = metric;
        Tensor dist = DistanceMatrix.of(sequence, msq);
        TensorUnaryOperator tuo = LogWeightings.COORDINATE.operator( //
            MetricBiinvariant.EUCLIDEAN, //
            homogeneousSpace, //
            InversePowerVariogram.of(2), sequence);
        TensorScalarFunction tsf = p -> {
          Tensor b = tuo.apply(p);
          return Abs.FUNCTION.apply((Scalar) dist.dot(b).dot(b));
        };
        Timing timing = Timing.started();
        Tensor matrix = hsArrayPlot.raster(resolution, tsf, DoubleScalar.INDETERMINATE);
        computeTime = timing.seconds();
        // ---
        if (jToggleThresh.isSelected())
          matrix = matrix.map(Round.FUNCTION); // effectively maps to 0 or 1
        // ---
        ColorDataGradient colorDataGradient = spinnerColorData.getValue();
        return ArrayPlotRender.rescale(matrix, colorDataGradient, spinnerMagnif.getValue()).export();
      } catch (Exception exception) {
        System.out.println(exception);
        exception.printStackTrace();
      }
    return null;
  }

  @Override
  public final void protected_render(GeometricLayer geometricLayer, Graphics2D graphics) {
    RenderQuality.setQuality(graphics);
    prepare();
    // ---
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    Tensor sequence = getGeodesicControlPoints();
    // Tensor values = getControlPointsSe2().get(Tensor.ALL, 2);
    BufferedImage bufferedImage = cache.apply(sequence);
    if (Objects.nonNull(bufferedImage)) {
      RenderQuality.setDefault(graphics); // default so that raster becomes visible
      Tensor pixel2model = HsArrayPlots.pixel2model( //
          manifoldDisplay.coordinateBoundingBox(), //
          new Dimension(bufferedImage.getHeight(), bufferedImage.getHeight()));
      ImageRender.of(bufferedImage, pixel2model).render(geometricLayer, graphics);
    }
    RenderQuality.setQuality(graphics);
    // renderControlPoints(geometricLayer, graphics);
    LeversRender leversRender = //
        LeversRender.of(manifoldDisplay, sequence, null, geometricLayer, graphics);
    leversRender.renderSequence();
    graphics.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
    graphics.setColor(Color.GRAY);
    graphics.drawString("compute: " + RealScalar.of(computeTime).map(Round._3), 0, 30);
  }

  void prepare() {
    // ---
  }

  public static void main(String[] args) {
    LookAndFeels.LIGHT.updateUI();
    new MaAveragingDemo().setVisible(1300, 800);
  }
}