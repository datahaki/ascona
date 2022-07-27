// code by jph
package ch.alpine.ascona.gbc.d2;

import java.util.function.Supplier;

import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.hs.sn.SnFastMean;
import ch.alpine.sophus.hs.sn.SnManifold;
import ch.alpine.sophus.hs.sn.SnPhongMean;
import ch.alpine.tensor.sca.Chop;

/** RMF(p,t,w)[x] == w.t for w = IDC(p,x) */
/* package */ enum SnMeans implements Supplier<BiinvariantMean> {
  EXACT(SnManifold.INSTANCE.biinvariantMean(Chop._03)),
  FAST(SnFastMean.INSTANCE),
  PHONG(SnPhongMean.INSTANCE);

  private final BiinvariantMean biinvariantMean;

  SnMeans(BiinvariantMean biinvariantMean) {
    this.biinvariantMean = biinvariantMean;
  }

  @Override
  public BiinvariantMean get() {
    return biinvariantMean;
  }
}
