// code by jph
package ch.alpine.ascona.usr;

import ch.alpine.bridge.fig.ListLinePlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.ShowDialog;
import ch.alpine.bridge.fig.Showable;
import ch.alpine.sophus.math.noise.ColoredNoise;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.pdf.RandomVariate;

/* package */ enum ColoredNoiseExport {
  ;
  public static void main(String[] args) {
    Show show = new Show();
    for (Tensor _x : Subdivide.of(0, 2, 10)) {
      ColoredNoise coloredNoise = new ColoredNoise(((Scalar) _x).number().doubleValue());
      Tensor tensor = RandomVariate.of(coloredNoise, 1000);
      Showable showable = ListLinePlot.of(Range.of(0, tensor.length()), tensor);
      showable.setLabel("" + _x);
      show.add(showable);
    }
    ShowDialog.of(show);
  }
}
