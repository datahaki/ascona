// code by jph
package ch.alpine.ascona.ref.d1h;

import ch.alpine.bridge.fig.ListPlot;
import ch.alpine.bridge.fig.Show;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.sca.pow.Power;

/* package */ enum StaticHelper {
  ;
  public static Show listPlot(Tensor deltas, Scalar delta, int levels) {
    Tensor domain = Range.of(0, deltas.length()).multiply(delta.divide(Power.of(2, levels)));
    Show show = new Show();
    int dims = Unprotect.dimension1Hint(deltas);
    for (int index = 0; index < dims; ++index)
      show.add(new ListPlot(domain, deltas.get(Tensor.ALL, index)));
    return show;
  }
}
