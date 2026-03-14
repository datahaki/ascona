// code by jph
package ch.alpine.ascona.dv;

import ch.alpine.sophis.dv.Biinvariant;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.lie.Symmetrize;

/* package */ enum StaticHelper {
  ;
  public static Tensor distanceMatrix_symmetrized(Biinvariant biinvariant, Tensor sequence) {
    return Symmetrize.of(distanceMatrix(biinvariant, sequence));
  }

  public static Tensor distanceMatrix(Biinvariant biinvariant, Tensor sequence) {
    return biinvariant.relative_distances(sequence).sunder().slash(sequence);
  }
}
