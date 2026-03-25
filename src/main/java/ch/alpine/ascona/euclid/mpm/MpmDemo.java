// code by jph
package ch.alpine.ascona.euclid.mpm;

import java.awt.Container;

import ch.alpine.bridge.fig.Show;
import ch.alpine.bridge.fig.ShowGridComponent;
import ch.alpine.bridge.fig.plt.ListPlot;
import ch.alpine.bridge.pro.ManipulateProvider;
import ch.alpine.bridge.ref.ann.FieldFuse;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;

@ReflectionMarker
class MpmDemo implements ManipulateProvider {
  @FieldFuse
  public Boolean fuse = true;
  final MPM2D mpm2d = new MPM2D();

  @Override
  public Container getContainer() {
    mpm2d.simulate();
    Tensor points = Tensor.of(mpm2d.particles.stream().map(p -> Tensors.vector(p.x, p.y)));
    Show show = new Show();
    show.add(ListPlot.of(points));
    return ShowGridComponent.of(show);
  }

  static void main() {
    new MpmDemo().runStandalone();
  }
}
