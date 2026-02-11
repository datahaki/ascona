// code by jph
package ch.alpine.ascona.ext;

import java.util.List;

import javax.swing.JComponent;

import ch.alpine.bridge.fig.ListLinePlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.ShowGridComponent;
import ch.alpine.bridge.fig.Spectrogram;
import ch.alpine.bridge.pro.ManipulateProvider;
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

@ReflectionMarker
public class ColoredNoiseDemo implements ManipulateProvider {
  @FieldSlider
  @FieldClip(min = "0", max = "2")
  @FieldPreferredWidth(250)
  public Scalar alpha = RealScalar.of(2);
  @FieldPreferredWidth(150)
  public Integer length = 300;
  @FieldFuse
  public transient Boolean generate = true;

  @Override
  public JComponent getJComponent() {
    ColoredNoise coloredNoise = new ColoredNoise(alpha.number().doubleValue());
    Tensor values = RandomVariate.of(coloredNoise, length);
    Tensor domain = Range.of(0, values.length());
    Show show1 = new Show();
    {
      show1.setPlotLabel("Signal");
      show1.add(ListLinePlot.of(domain, values));
    }
    Show show2 = new Show();
    {
      show2.add(Spectrogram.of(values, RealScalar.ONE));
    }
    return ShowGridComponent.of(List.of(show1, show2));
  }

  static void main() {
    new ColoredNoiseDemo().run();
  }
}
