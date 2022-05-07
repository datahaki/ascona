// code by jph
package ch.alpine.ascona.util.sym;

import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.itp.BinaryAverage;

/** characterized by {@link #isNode()} == true */
public class SymLinkLeaf extends SymLink {
  private final Scalar scalar;
  /** position is assigned in sym link builder */
  Tensor position;

  /* package */ SymLinkLeaf(Scalar scalar) {
    this.scalar = scalar;
  }

  @Override // from SymLink
  public int getIndex() {
    return scalar.number().intValue();
  }

  @Override // from SymLink
  public Tensor getPosition() {
    return Tensors.of(scalar, RealScalar.ZERO);
  }

  @Override // from SymLink
  public Tensor getPosition(BinaryAverage binaryAverage) {
    return position;
  }

  @Override
  public boolean isNode() {
    return true;
  }

  @Override
  public int depth() {
    return 0;
  }
}
