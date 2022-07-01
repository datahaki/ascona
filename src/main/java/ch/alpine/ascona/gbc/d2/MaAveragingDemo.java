// code by jph
package ch.alpine.ascona.gbc.d2;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JToggleButton;

import ch.alpine.ascona.gbc.AnAveragingDemo;
import ch.alpine.ascona.util.arp.ArrayFunction;
import ch.alpine.ascona.util.arp.ArrayPlotRender;
import ch.alpine.ascona.util.arp.D2Raster;
import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.ascona.util.ren.ImageRender;
import ch.alpine.ascona.util.ren.LeversRender;
import ch.alpine.bridge.awt.RenderQuality;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.swing.LookAndFeels;
import ch.alpine.bridge.swing.SpinnerLabel;
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

/** TODO ASCONA reference marc alexa */
public class MaAveragingDemo extends AnAveragingDemo {
  private final SpinnerLabel<ColorDataGradients> spinnerColorData = SpinnerLabel.of(ColorDataGradients.class);
  private final SpinnerLabel<Integer> spinnerRes = SpinnerLabel.of(20, 30, 40, 50, 75, 100, 150, 200, 250);
  private final JToggleButton jToggleThresh = new JToggleButton("thresh");

  public MaAveragingDemo() {
    super(ManifoldDisplays.d2Rasters());
    {
      spinnerColorData.setValue(ColorDataGradients.PARULA);
      spinnerColorData.addToComponentReduced(timerFrame.jToolBar, new Dimension(200, 28), "color scheme");
      spinnerColorData.addSpinnerListener(v -> recompute());
    }
    {
      spinnerRes.setValue(30);
      spinnerRes.addToComponentReduced(timerFrame.jToolBar, new Dimension(60, 28), "resolution");
      spinnerRes.addSpinnerListener(v -> recompute());
    }
    {
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
    Tensor sequence = tensor.map(N.DOUBLE);
    int resolution = spinnerRes.getValue();
    int n = sequence.length();
    if (2 < n)
      try {
        ManifoldDisplay manifoldDisplay = manifoldDisplay();
        D2Raster d2Raster = (D2Raster) manifoldDisplay;
        HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay().geodesicSpace();
        final Tensor dist;
        if (jToggleThresh.isSelected() || !(homogeneousSpace instanceof TensorMetric)) {
          dist = ConstantArray.of(RealScalar.ONE, n, n).subtract(IdentityMatrix.of(n));
        } else {
          TensorMetric tensorMetric = (TensorMetric) homogeneousSpace;
          TensorMetric msq = (p, q) -> AbsSquared.FUNCTION.apply(tensorMetric.distance(p, q));
          dist = DistanceMatrix.of(sequence, msq);
        }
        Sedarim sedarim = biinvariant().coordinate(InversePowerVariogram.of(2), sequence);
        TensorScalarFunction tsf = p -> {
          Tensor b = sedarim.sunder(p);
          return Abs.FUNCTION.apply((Scalar) dist.dot(b).dot(b));
        };
        Timing timing = Timing.started();
        ArrayFunction<Scalar> arrayFunction = new ArrayFunction<>(tsf, DoubleScalar.INDETERMINATE);
        Tensor matrix = D2Raster.of(d2Raster, resolution, arrayFunction);
        computeTime = timing.seconds();
        // ---
        ColorDataGradient colorDataGradient = spinnerColorData.getValue();
        return ArrayPlotRender.rescale(matrix, colorDataGradient, 1, false);
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
    ArrayPlotRender arrayPlotRender = cache.apply(sequence);
    if (Objects.nonNull(arrayPlotRender)) {
      RenderQuality.setDefault(graphics); // default so that raster becomes visible
      D2Raster hsArrayPlot = (D2Raster) manifoldDisplay;
      new ImageRender(arrayPlotRender.bufferedImage(), hsArrayPlot.coordinateBoundingBox()) //
          .render(geometricLayer, graphics);
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
    LookAndFeels.LIGHT.updateComponentTreeUI();
    new MaAveragingDemo().setVisible(1300, 800);
  }
}
