// code by jph
package ch.alpine.ascona.dv;

import ch.alpine.sophis.dv.Biinvariant;
import ch.alpine.tensor.Slash;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.lie.Symmetrize;

enum StaticHelper {
  ;
  public static Tensor distanceMatrix_symmetrized(Biinvariant biinvariant, Tensor sequence) {
    return Symmetrize.of(distanceMatrix(biinvariant, sequence));
  }

  public static Tensor distanceMatrix(Biinvariant biinvariant, Tensor sequence) {
    return Slash.of(sequence, biinvariant.relative_distances(sequence).sunder());
  }
}
