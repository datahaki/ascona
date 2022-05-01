// code by jph
package ch.alpine.ascona.sym;

import java.io.Serializable;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import ch.alpine.sophus.lie.rn.RnGeodesic;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.TensorRuntimeException;
import ch.alpine.tensor.Tensors;

public class SymScalar extends ScalarAdapter implements Serializable {
  /** @param p
   * @param q
   * @param ratio
   * @return */
  public static Scalar of(Scalar p, Scalar q, Scalar ratio) {
    if (p instanceof SymScalar && q instanceof SymScalar)
      return new SymScalar(Tensors.of(p, q, ratio).unmodifiable());
    throw TensorRuntimeException.of(p, q, ratio);
  }

  /** @param number of control coordinate
   * @return */
  public static Scalar leaf(int number) {
    return new SymScalar(RealScalar.of(number));
  }
  // public static Scalar leaf(Scalar scalar) {
  // return new SymScalar(scalar);
  // }

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
