// code by jph
package ch.alpine.ascona.ref.d1h;

import ch.alpine.bridge.fig.JFreeChart;
import ch.alpine.bridge.fig.ListPlot;
import ch.alpine.bridge.fig.VisualSet;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Unprotect;
import ch.alpine.tensor.alg.Range;
import ch.alpine.tensor.sca.pow.Power;

/* package */ enum StaticHelper {
  ;
  public static JFreeChart listPlot(Tensor deltas, Scalar delta, int levels) {
    Tensor domain = Range.of(0, deltas.length()).multiply(delta.divide(Power.of(2, levels)));
    VisualSet visualSet = new VisualSet();
    int dims = Unprotect.dimension1Hint(deltas);
    for (int index = 0; index < dims; ++index)
      visualSet.add(domain, deltas.get(Tensor.ALL, index));
    return ListPlot.of(visualSet);
  }
}
