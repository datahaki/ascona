// code by jph
package ch.alpine.ascona.gbc.d2;

import java.util.function.Supplier;

import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.hs.h.HManifold;

/** RMF(p,t,w)[x] == w.t for w = IDC(p,x) */
/* package */ enum HnMeans implements Supplier<BiinvariantMean> {
  EXACT(HManifold.INSTANCE.biinvariantMean()),
  // FAST(HnFastMean.INSTANCE),
  // PHONG(HnPhongMean.INSTANCE),
  ;

  private final BiinvariantMean biinvariantMean;

  HnMeans(BiinvariantMean biinvariantMean) {
    this.biinvariantMean = biinvariantMean;
  }

  @Override
  public BiinvariantMean get() {
    return biinvariantMean;
  }
}
