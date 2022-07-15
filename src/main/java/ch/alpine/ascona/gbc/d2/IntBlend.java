// code by jph
package ch.alpine.ascona.gbc.d2;

import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.red.Min;
import ch.alpine.tensor.sca.Abs;
import ch.alpine.tensor.sca.Round;

public record IntBlend(Scalar radius) implements ScalarUnaryOperator {
  @Override
  public Scalar apply(Scalar scalar) {
    return Min.of(Abs.between(scalar, Round.FUNCTION.apply(scalar)).divide(radius), RealScalar.ONE);
  }
}
