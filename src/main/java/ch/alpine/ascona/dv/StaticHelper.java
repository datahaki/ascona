// code by jph
package ch.alpine.ascona.dv;

import ch.alpine.sophus.dv.Biinvariants;
import ch.alpine.sophus.hs.Manifold;
import ch.alpine.sophus.hs.Sedarim;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.lie.Symmetrize;
import ch.alpine.tensor.mat.SymmetricMatrixQ;

/* package */ enum StaticHelper {
  ;
  public static Tensor distanceMatrix(Manifold manifold, Tensor sequence) {
    Tensor matrix = distanceMatrix(manifold, sequence, sequence);
    return SymmetricMatrixQ.of(matrix) //
        ? matrix
        : Symmetrize.of(matrix);
  }

  public static Tensor distanceMatrix(Manifold manifold, Tensor sequence, Tensor target) {
    Sedarim sedarim = Biinvariants.METRIC.ofSafe(manifold).distances(target);
    return Tensor.of(sequence.stream().map(sedarim::sunder));
  }
}
