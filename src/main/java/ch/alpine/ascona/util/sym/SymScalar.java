// code by jph
package ch.alpine.ascona.util.sym;

import java.io.Serializable;

import ch.alpine.tensor.MultiplexScalar;
import ch.alpine.tensor.Scalar;

public abstract class SymScalar extends MultiplexScalar implements Serializable {
  /** @return recursive evaluation of this coordinate */
  protected abstract Scalar evaluate();

  public abstract SymLink build();

  @Override
  public Scalar multiply(Scalar scalar) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Scalar negate() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Scalar reciprocal() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Scalar zero() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Scalar one() {
    throw new UnsupportedOperationException();
  }

  @Override
  protected Scalar plus(Scalar scalar) {
    throw new UnsupportedOperationException();
  }
}
