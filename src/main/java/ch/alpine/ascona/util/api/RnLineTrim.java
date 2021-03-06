// code by jph
package ch.alpine.ascona.util.api;

import ch.alpine.sophus.crv.d2.TripleReduceExtrapolation;
import ch.alpine.sophus.lie.rn.RnLineDistance;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;

// TODO ASCONA name is not good
public enum RnLineTrim {
  ;
  public static final TripleReduceExtrapolation TRIPLE_REDUCE_EXTRAPOLATION = new TripleReduceExtrapolation() {
    @Override
    protected Scalar reduce(Tensor p, Tensor q, Tensor r) {
      return RnLineDistance.INSTANCE.tensorNorm(p, r).norm(q);
    }
  };
}
