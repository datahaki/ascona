// code by jph
package ch.alpine.ascona.gbc.d2;

import java.util.function.Supplier;

import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.hs.s.SnFastMean;
import ch.alpine.sophus.hs.s.SnManifold;
import ch.alpine.sophus.hs.s.SnPhongMean;

/** RMF(p,t,w)[x] == w.t for w = IDC(p,x) */
/* package */ enum SnMeans implements Supplier<BiinvariantMean> {
  EXACT(SnManifold.INSTANCE.biinvariantMean()),
  FAST(SnFastMean.INSTANCE::estimate),
  PHONG(SnPhongMean.INSTANCE::estimate);

  private final BiinvariantMean biinvariantMean;

  SnMeans(BiinvariantMean biinvariantMean) {
    this.biinvariantMean = biinvariantMean;
  }

  @Override
  public BiinvariantMean get() {
    return biinvariantMean;
  }
}
