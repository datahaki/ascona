// code by jph
package ch.alpine.ascona.util.sym;

import ch.alpine.sophus.lie.rn.RnGroup;
import ch.alpine.tensor.RealScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.itp.BinaryAverage;
import ch.alpine.tensor.red.Min;

public class SymLinkPart extends SymLink {
  private static final Scalar SHIFT_Y = RealScalar.of(0.5);
  public final SymLink lP;
  public final SymLink lQ;
  public final Scalar lambda;

  SymLinkPart(SymLink lP, SymLink lQ, Scalar lambda) {
    this.lP = lP;
    this.lQ = lQ;
    this.lambda = lambda;
  }

  @Override
  public boolean isNode() {
    return false;
  }

  @Override
  public int depth() {
    return Math.max(lP.depth(), lQ.depth()) + 1;
  }

  @Override
  public Tensor getPosition() {
    Tensor posP = lP.getPosition();
    Tensor posQ = lQ.getPosition();
    return Tensors.of( //
        RnGroup.INSTANCE.split(posP.Get(0), posQ.Get(0), lambda), //
        Min.of(posP.Get(1), posQ.Get(1)).subtract(SHIFT_Y));
  }

  @Override
  public Tensor getPosition(BinaryAverage binaryAverage) {
    return binaryAverage.split( //
        lP.getPosition(binaryAverage), //
        lQ.getPosition(binaryAverage), //
        lambda);
  }
}
