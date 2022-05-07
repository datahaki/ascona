// code by jph
package ch.alpine.ascona.util.sym;

import java.io.Serializable;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

import ch.alpine.sophus.lie.rn.RnGeodesic;
import ch.alpine.tensor.MultiplexScalar;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.TensorRuntimeException;
import ch.alpine.tensor.Tensors;

// TODO ASCONA this class is two classes in one
public class SymScalar extends MultiplexScalar implements Serializable {
  /** @param length
   * @return vector of given length and node entries indexed 0, 1, 2, ..., length - 1 */
  public static Tensor init(int length) {
    return Tensor.of(IntStream.range(0, length).mapToObj(SymScalar::leaf));
  }

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
  public Tensor tensor() {
    return tensor;
  }

  public boolean isScalar() {
    return tensor instanceof Scalar;
  }

  public SymScalar getP() {
    return (SymScalar) tensor.Get(0);
  }

  public SymScalar getQ() {
    return (SymScalar) tensor.Get(1);
  }

  public Scalar ratio() {
    return tensor.Get(2);
  }

  public Scalar evaluate() {
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
    if (object instanceof SymScalar) {
      SymScalar symScalar = (SymScalar) object;
      return symScalar.tensor.equals(tensor);
    }
    return false;
  }

  @Override
  public String toString() {
    return tensor.toString();
  }
}
