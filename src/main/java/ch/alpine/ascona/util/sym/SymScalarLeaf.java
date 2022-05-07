// code by jph
package ch.alpine.ascona.util.sym;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;

/* package */ class SymScalarLeaf extends SymScalar {
  /** @param index
   * @return */
  public static Scalar of(int index) {
    return new SymScalarLeaf(RealScalar.of(index));
  }

  // ---
  private final Scalar scalar;

  private SymScalarLeaf(Scalar scalar) {
    this.scalar = scalar;
  }

  @Override
  protected Scalar evaluate() {
    return scalar;
  }

  @Override
  public SymLink build() {
    return new SymLinkLeaf(scalar);
  }

  @Override
  public Scalar eachMap(UnaryOperator<Scalar> unaryOperator) {
    return new SymScalarLeaf(unaryOperator.apply(scalar));
  }

  @Override
  public boolean allMatch(Predicate<Scalar> predicate) {
    return predicate.test(scalar);
  }

  @Override
  public int hashCode() {
    return scalar.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    return object instanceof SymScalarLeaf symScalar //
        && symScalar.scalar.equals(scalar);
  }

  @Override
  public String toString() {
    return scalar.toString();
  }
}
