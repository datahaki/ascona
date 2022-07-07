// code by jph
package ch.alpine.ascona.misc;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import org.jfree.chart.JFreeChart;

import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.bridge.fig.ListPlot;
import ch.alpine.bridge.fig.Spectrogram;
import ch.alpine.bridge.fig.VisualSet;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.FieldInteger;
import ch.alpine.bridge.ref.ann.FieldPreferredWidth;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.bridge.swing.LookAndFeels;
import ch.alpine.sophus.math.noise.ColoredNoise;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.pdf.RandomVariate;
import ch.alpine.tensor.sca.Clips;

// TODO ASCONA separate standalone demos (like this one) from geometric/manifold demos
public class ColoredNoiseDemo extends AbstractDemo {
  @ReflectionMarker
  public static class Param {
    @FieldSlider
    @FieldClip(min = "0", max = "2")
    @FieldPreferredWidth(250)
    public Scalar alpha = RealScalar.of(2);
    @FieldPreferredWidth(150)
    @FieldInteger
    public Scalar length = RealScalar.of(300);
    @FieldFuse("generate")
    public transient Boolean generate = true;
  }

  private final Param param;
  private JFreeChart jFreeChart;
  private JFreeChart spectrogra;

  public ColoredNoiseDemo() {
    this(new Param());
  }

  public ColoredNoiseDemo(Param param) {
    super(param);
    this.param = param;
    fieldsEditor.addUniversalListener(this::compute);
    // ---
    timerFrame.geometricComponent.setRotatable(false);
    compute();
  }

  private void compute() {
    ColoredNoise coloredNoise = new ColoredNoise(param.alpha.number().doubleValue());
    Tensor values = RandomVariate.of(coloredNoise, param.length.number().intValue());
    Tensor domain = Range.of(0, values.length());
    {
      VisualSet visualSet = new VisualSet();
      visualSet.add(domain, values);
      visualSet.getAxisX().setClip(Clips.interval(0, values.length() - 1));
      jFreeChart = ListPlot.of(visualSet, true);
    }
    {
      VisualSet visualSet = new VisualSet();
      visualSet.add(domain, values);
      spectrogra = Spectrogram.of(visualSet);
    }
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    float width = geometricLayer.model2pixelWidth(RealScalar.of(15));
    int piw = (int) width;
    int pih = (int) (width * 0.4);
    jFreeChart.draw(graphics, new Rectangle(0, 0, piw, pih));
    spectrogra.draw(graphics, new Rectangle(0, pih, piw, pih));
  }

  public static void main(String[] args) {
    LookAndFeels.LIGHT.updateComponentTreeUI();
    new ColoredNoiseDemo().setVisible(1000, 800);
  }
}
