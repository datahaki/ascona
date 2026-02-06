// code by jph
package ch.alpine.ascona.usr;

import ch.alpine.bridge.fig.ListPlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.ShowDialog;
import ch.alpine.sophus.math.sample.BallRandomSample;
import ch.alpine.sophus.math.sample.RingRandomSample;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.pdf.RandomSample;
import ch.alpine.tensor.pdf.RandomSampleInterface;

/* package */ enum BallRandomSampleDemo {
  ;
  static void main() {
    RandomSampleInterface randomSampleInterface = //
        BallRandomSample.of(Tensors.vector(10, 1), RealScalar.of(2));
    randomSampleInterface = //
        new RingRandomSample(2, RealScalar.of(1.5), RealScalar.of(3));
    Tensor matrix = RandomSample.of(randomSampleInterface, 10000);
    Show show = new Show();
    show.add(ListPlot.of(matrix));
    show.setAspectRatio(RealScalar.ONE);
    ShowDialog.of(show);
  }
}
