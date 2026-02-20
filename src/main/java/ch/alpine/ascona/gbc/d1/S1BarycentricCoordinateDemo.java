// code by jph
package ch.alpine.ascona.gbc.d1;

import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.alg.Subdivide;
import ch.alpine.tensor.lie.rot.AngleVector;
import ch.alpine.tensor.num.Pi;

// TODO ASCONA ALG crashes when only 2 control points exist
public class S1BarycentricCoordinateDemo extends A1BarycentricCoordinateDemo {
  @Override
  Tensor domain(Tensor support) {
    return Subdivide.of(Pi.VALUE.negate(), Pi.VALUE, 128);
  }

  @Override
  Tensor lift(Scalar x) {
    return AngleVector.of(x);
  }

  static void main() {
    new S1BarycentricCoordinateDemo().runStandalone();
  }
}
