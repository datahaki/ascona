// code by jph
package ch.alpine.euclid.hil;

import java.awt.Container;

import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.ShowGridComponent;
import ch.alpine.bridge.pro.ManipulateProvider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.img.ColorDataGradients;

@ReflectionMarker
class HilbertCoordinateShow implements ManipulateProvider {
  public Integer levels = 3;
  public ColorDataGradients cdg = ColorDataGradients.GREEN_BROWN_TERRAIN;

  @Override
  public Container getContainer() {
    Tensor sequence = HilbertBenchmarkDemo.unit(levels);
    int res = 1 << levels;
    Show show = HilbertLevelShow.of(sequence, res, cdg, 800);
    return ShowGridComponent.of(show);
  }

  static void main() {
    new HilbertCoordinateShow().runStandalone();
  }
}
