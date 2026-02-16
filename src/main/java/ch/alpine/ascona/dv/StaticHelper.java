// code by jph
package ch.alpine.ascona.dv;

import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophis.dv.Sedarim;
import ch.alpine.sophus.math.api.Manifold;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.lie.Symmetrize;
import ch.alpine.tensor.mat.SymmetricMatrixQ;

/* package */ enum StaticHelper {
  ;
  public static Tensor distanceMatrix(Manifold manifold, Tensor sequence) {
    Tensor matrix = distanceMatrix(manifold, sequence, sequence);
    return SymmetricMatrixQ.INSTANCE.test(matrix) //
        ? matrix
        : Symmetrize.of(matrix);
  }

  public static Tensor distanceMatrix(Manifold manifold, Tensor sequence, Tensor target) {
    Sedarim sedarim = Biinvariants.METRIC.ofSafe(manifold).relative_distances(target);
    return Tensor.of(sequence.stream().map(sedarim::sunder));
  }
}
