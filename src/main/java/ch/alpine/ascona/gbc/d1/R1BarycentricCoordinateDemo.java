// code by jph
package ch.alpine.ascona.gbc.d1;

import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.red.Max;
import ch.alpine.tensor.red.Min;
import ch.alpine.tensor.sca.N;

public class R1BarycentricCoordinateDemo extends A1BarycentricCoordinateDemo {
  private static final Scalar MARGIN = RealScalar.of(2);

  @Override
  Tensor domain(Tensor support) {
    return Subdivide.of( //
        support.stream().reduce(Min::of).orElseThrow().add(MARGIN.negate()), //
        support.stream().reduce(Max::of).orElseThrow().add(MARGIN), 128).map(N.DOUBLE);
  }

  @Override
  Tensor lift(Scalar x) {
    return Tensors.of(x);
  }

  public static void main(String[] args) {
    launch();
  }
}
