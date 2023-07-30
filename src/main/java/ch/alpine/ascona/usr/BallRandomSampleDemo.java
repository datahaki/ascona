// code by jph
package ch.alpine.ascona.usr;

import ch.alpine.bridge.fig.ListPlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.ShowDialog;
import ch.alpine.sophus.math.sample.BallRandomSample;
import ch.alpine.sophus.math.sample.RandomSample;
import ch.alpine.sophus.math.sample.RandomSampleInterface;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;

/* package */ enum BallRandomSampleDemo {
  ;
  public static void main(String[] args) {
    RandomSampleInterface randomSampleInterface = //
        BallRandomSample.of(Tensors.vector(10, 1), RealScalar.of(2));
    Tensor matrix = RandomSample.of(randomSampleInterface, 10000);
    Show show = new Show();
    show.add(ListPlot.of(matrix));
    ShowDialog.of(show);
  }
}
