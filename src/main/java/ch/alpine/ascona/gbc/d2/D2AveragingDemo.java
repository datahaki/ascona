// code by jph
package ch.alpine.ascona.gbc.d2;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JToggleButton;

import org.jfree.chart.JFreeChart;

import ch.alpine.ascona.gbc.AnAveragingDemo;
import ch.alpine.ascona.util.arp.ArrayFunction;
import ch.alpine.ascona.util.arp.ArrayPlotRender;
import ch.alpine.ascona.util.arp.D2Raster;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.ImageRender;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.fig.ArrayPlot;
import ch.alpine.bridge.fig.VisualImage;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.swing.LookAndFeels;
import ch.alpine.bridge.swing.SpinnerLabel;
import ch.alpine.tensor.DoubleScalar;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorScalarFunction;
import ch.alpine.tensor.ext.Cache;
import ch.alpine.tensor.ext.Timing;
import ch.alpine.tensor.img.ColorDataGradient;
import ch.alpine.tensor.img.ColorDataGradients;
import ch.alpine.tensor.opt.nd.CoordinateBoundingBox;
import ch.alpine.tensor.sca.N;
import ch.alpine.tensor.sca.Round;

public class D2AveragingDemo extends AnAveragingDemo {
  private final SpinnerLabel<Scalar> spinnerCvar;
  private final SpinnerLabel<Integer> spinnerMagnif;
  private final SpinnerLabel<ColorDataGradients> spinnerColorData = SpinnerLabel.of(ColorDataGradients.class);
  private final SpinnerLabel<Integer> spinnerRes = SpinnerLabel.of(20, 30, 40, 50, 75, 100, 150, 200, 250);
  private final JToggleButton jToggleVarian = new JToggleButton("est/var");
  private final JToggleButton jToggleThresh = new JToggleButton("thresh");

  public D2AveragingDemo() {
    super(ManifoldDisplays.d2Rasters());
    {
      spinnerCvar = SpinnerLabel.of(Tensors.fromString("{0, 0.01, 0.1, 0.5, 1}").stream().map(Scalar.class::cast).toList());
      spinnerCvar.setValue(RealScalar.ZERO);
      spinnerCvar.addToComponentReduced(timerFrame.jToolBar, new Dimension(60, 28), "error");
    }
    {
      spinnerMagnif = SpinnerLabel.of(1, 2, 3, 4, 5, 6, 7, 8);
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
    addManifoldListener(v -> recompute());
    timerFrame.geometricComponent.setOffset(400, 400);
    // ---
    setControlPointsSe2(Tensors.fromString("{{0, 0, 1}, {1, 0, 1}, {-1, 1, 0}, {-0.5, -1, 0}, {0.4, 1, 0}}"));
  }

  private static final int CACHE_SIZE = 1;
  private final Cache<Tensor, ArrayPlotRender> cache = Cache.of(this::computeImage, CACHE_SIZE);
  private double computeTime = 0;

  @Override
  protected final void recompute() {
    System.out.println("clear");
    cache.clear();
  }

  private final ArrayPlotRender computeImage(Tensor tensor) {
    Tensor sequence = tensor.get(0).map(N.DOUBLE);
    Tensor values = tensor.get(1).map(N.DOUBLE);
    int resolution = spinnerRes.getValue();
    if (2 < values.length())
      try {
        ManifoldDisplay manifoldDisplay = manifoldDisplay();
        TensorScalarFunction tensorScalarFunction = function(sequence, values);
        D2Raster hsArrayPlot = (D2Raster) manifoldDisplay;
        ScalarUnaryOperator suo = Round.toMultipleOf(RationalScalar.of(2, 10));
        TensorScalarFunction tsf = t -> suo.apply(tensorScalarFunction.apply(t));
        Timing timing = Timing.started();
        ArrayFunction<Scalar> arrayFunction = new ArrayFunction<>(tsf, DoubleScalar.INDETERMINATE);
        Tensor matrix = D2Raster.of(hsArrayPlot, resolution, arrayFunction);
        computeTime = timing.seconds();
        // ---
        if (jToggleThresh.isSelected())
          matrix = matrix.map(Round.FUNCTION); // effectively maps to 0 or 1
        // ---
        ColorDataGradient colorDataGradient = spinnerColorData.getValue();
        return ArrayPlotRender.rescale(matrix, colorDataGradient, spinnerMagnif.getValue(), false);
      } catch (Exception exception) {
        System.out.println(exception);
        exception.printStackTrace();
      }
    return null;
  }

  @Override
  public final void protected_render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Dimension dimension = timerFrame.geometricComponent.jComponent.getSize();
    RenderQuality.setQuality(graphics);
    prepare();
    // ---
    ManifoldDisplay manifoldDisplay = manifoldDisplay();
    D2Raster d2Raster = (D2Raster) manifoldDisplay;
    CoordinateBoundingBox coordinateBoundingBox = d2Raster.coordinateBoundingBox();
    Tensor sequence = getGeodesicControlPoints();
    Tensor values = getControlPointsSe2().get(Tensor.ALL, 2);
    ArrayPlotRender arrayPlotRender = cache.apply(Unprotect.byRef(sequence, values));
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
    // renderControlPoints(geometricLayer, graphics);
    LeversRender leversRender = //
        LeversRender.of(manifoldDisplay, sequence, values, geometricLayer, graphics);
    leversRender.renderSequence();
    leversRender.renderWeights(values);
    graphics.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
    graphics.setColor(Color.GRAY);
    graphics.drawString("compute: " + RealScalar.of(computeTime).map(Round._3), 0, 30);
  }

  void prepare() {
    // ---
  }

  public static void main(String[] args) {
    LookAndFeels.LIGHT.updateComponentTreeUI();
    new D2AveragingDemo().setVisible(1300, 800);
  }
}
