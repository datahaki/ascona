// code by jph
package ch.alpine.ascona.util.sym;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import ch.alpine.sophus.lie.rn.RnGroup;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensors;

/* package */ class SymScalarPart extends SymScalar {
  private final SymScalar p;
  private final SymScalar q;
  private final Scalar ratio;

  public SymScalarPart(Scalar p, Scalar q, Scalar ratio) {
    this.p = (SymScalar) p;
    this.q = (SymScalar) q;
    this.ratio = ratio;
  }

  /* package */ SymScalar getP() {
    return p;
  }

  /* package */ SymScalar getQ() {
    return q;
  }

  /* package */ Scalar ratio() {
    return ratio;
  }

  @Override
  protected Scalar evaluate() {
    return (Scalar) RnGroup.INSTANCE.split( //
        getP().evaluate(), //
        getQ().evaluate(), //
        ratio());
  }

  @Override
  public SymLink build() {
    return new SymLinkPart(p.build(), q.build(), ratio);
  }

  @Override
  public Scalar eachMap(UnaryOperator<Scalar> unaryOperator) {
    return new SymScalarPart(p, q, unaryOperator.apply(ratio));
  }

  @Override
  public boolean allMatch(Predicate<Scalar> predicate) {
    return predicate.test(ratio);
  }

  @Override
  public int hashCode() {
    return Objects.hash(p, q, ratio);
  }

  @Override
  public boolean equals(Object object) {
    return object instanceof SymScalarPart symScalar //
        && symScalar.p.equals(p) //
        && symScalar.q.equals(q) //
        && symScalar.ratio.equals(ratio);
  }

  @Override
  public String toString() {
    return Tensors.of(p, q, ratio).toString();
  }
}
