// code by jph
package ch.alpine.ascona.euclid.gbc;

import java.awt.BasicStroke;
import java.awt.Stroke;

import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Join;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.red.Max;
import ch.alpine.tensor.red.Min;
import ch.alpine.tensor.sca.N;

enum StaticHelper {
  ;
  static final Stroke STROKE = //
      new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3 }, 0);
  private static final Scalar MARGIN = RealScalar.of(2);

  public static Tensor domain(Tensor ctrlPointsSe2) {
    Tensor support = ctrlPointsSe2.get(Tensor.ALL, 0).maps(N.DOUBLE);
    Tensor subdiv = Subdivide.of( //
        support.stream().reduce(Min::of).orElseThrow().add(MARGIN.negate()), //
        support.stream().reduce(Max::of).orElseThrow().add(MARGIN), 100).maps(N.DOUBLE);
    Tensor predom = Join.of(subdiv, support);
    return Tensor.of(predom.stream().distinct().sorted());
  }
}
