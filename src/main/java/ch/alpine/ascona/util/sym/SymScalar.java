// code by jph
package ch.alpine.ascona.util.sym;

import java.io.Serializable;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import ch.alpine.sophus.lie.rn.RnGeodesic;
import ch.alpine.tensor.MultiplexScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.TensorRuntimeException;
import ch.alpine.tensor.Tensors;

// TODO ASCONA this class is two classes in one
public class SymScalar extends MultiplexScalar implements Serializable {
  /** @param p
   * @param q
   * @param ratio
   * @return */
  /* package */ static Scalar of(Scalar p, Scalar q, Scalar ratio) {
    if (p instanceof SymScalar && q instanceof SymScalar)
      return new SymScalar(Tensors.of(p, q, ratio).unmodifiable());
    throw TensorRuntimeException.of(p, q, ratio);
  }

  /** @param number of control coordinate
   * @return */
  /* package */ static Scalar leaf(int length) {
    return new SymScalar(RealScalar.of(length));
  }

  // ---
  private final Tensor tensor;

  private SymScalar(Tensor tensor) {
    this.tensor = tensor;
  }

  /** @return unmodifiable tensor */
  /* package */ Tensor tensor() {
    return tensor;
  }

  /* package */ boolean isScalar() {
    return tensor instanceof Scalar;
  }

  /* package */ SymScalar getP() {
    return (SymScalar) tensor.Get(0);
  }

  /* package */ SymScalar getQ() {
    return (SymScalar) tensor.Get(1);
  }

  /* package */ Scalar ratio() {
    return tensor.Get(2);
  }

  /* package */ Scalar evaluate() {
    return isScalar() //
        ? (Scalar) tensor
        : (Scalar) RnGeodesic.INSTANCE.split( //
            getP().evaluate(), //
            getQ().evaluate(), //
            ratio());
  }

  @Override
  public Scalar eachMap(UnaryOperator<Scalar> unaryOperator) {
    return isScalar() //
        ? unaryOperator.apply((Scalar) tensor)
        : of(getP(), getQ(), unaryOperator.apply(ratio()));
  }

  @Override
  public boolean allMatch(Predicate<Scalar> predicate) {
    return predicate.test(isScalar() //
        ? (Scalar) tensor
        : ratio());
  }

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

  @Override
  public int hashCode() {
    return tensor.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    return object instanceof SymScalar symScalar //
        && symScalar.tensor.equals(tensor);
  }

  @Override
  public String toString() {
    return tensor.toString();
  }
}
