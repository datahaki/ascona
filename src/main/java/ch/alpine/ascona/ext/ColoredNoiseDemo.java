// code by jph
package ch.alpine.ascona.ext;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.bridge.fig.ListPlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.Showable;
import ch.alpine.bridge.fig.Spectrogram;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.FieldPreferredWidth;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophus.math.noise.ColoredNoise;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.alg.Transpose;
import ch.alpine.tensor.pdf.RandomVariate;

public class ColoredNoiseDemo extends AbstractDemo {
  @ReflectionMarker
  public static class Param {
    @FieldSlider
    @FieldClip(min = "0", max = "2")
    @FieldPreferredWidth(250)
    public Scalar alpha = RealScalar.of(2);
    @FieldPreferredWidth(150)
    public Integer length = 300;
    @FieldFuse
    public transient Boolean generate = true;
  }

  private final Param param;
  private Showable jFreeChart;
  private Showable spectrogra;

  public ColoredNoiseDemo() {
    this(new Param());
  }

  public ColoredNoiseDemo(Param param) {
    super(param);
    this.param = param;
    fieldsEditor(0).addUniversalListener(this::compute);
    // ---
    timerFrame.geometricComponent.setRotatable(false);
    compute();
  }

  private void compute() {
    ColoredNoise coloredNoise = new ColoredNoise(param.alpha.number().doubleValue());
    Tensor values = RandomVariate.of(coloredNoise, param.length);
    Tensor domain = Range.of(0, values.length());
    {
      //
      jFreeChart = ListPlot.of(domain, values);
      // visualSet.getAxisX().setClip(Clips.interval(0, values.length() - 1));
      // jFreeChart = ListPlot.of(visualSet.setJoined(true));
    }
    {
      Tensor points = Transpose.of(Tensors.of(domain, values));
      spectrogra = Spectrogram.of(points);
    }
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    float width = geometricLayer.model2pixelWidth(RealScalar.of(15));
    int piw = (int) width;
    int pih = (int) (width * 0.4);
    {
      Show show = new Show();
      show.add(jFreeChart);
      show.render(graphics, new Rectangle(0, 0, piw, pih));
    }
    {
      Show show = new Show();
      show.add(spectrogra);
      show.render(graphics, new Rectangle(0, pih, piw, pih));
    }
  }

  public static void main(String[] args) {
    launch();
  }
}
