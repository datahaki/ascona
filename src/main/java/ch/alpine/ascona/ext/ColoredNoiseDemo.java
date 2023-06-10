// code by jph
package ch.alpine.ascona.ext;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import ch.alpine.ascona.util.win.AbstractDemo;
import ch.alpine.bridge.fig.ListLinePlot;
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
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.pdf.RandomVariate;

// TODO ASCONA why is there animation storage icon here?
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
  private Showable showable;
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
      showable = ListLinePlot.of(domain, values);
    }
    {
      spectrogra = Spectrogram.of(values, RealScalar.ONE);
    }
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Dimension dimension = timerFrame.geometricComponent.jComponent.getSize();
    int height = dimension.height / 2;
    {
      Show show = new Show();
      show.setPlotLabel("Signal");
      show.add(showable);
      show.render_autoIndent(graphics, new Rectangle(0, 0, dimension.width, height));
    }
    {
      Show show = new Show();
      show.setPlotLabel("Spectrogram");
      show.add(spectrogra);
      show.render_autoIndent(graphics, new Rectangle(0, height, dimension.width, height));
    }
  }

  public static void main(String[] args) {
    launch();
  }
}
