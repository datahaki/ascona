// code by jph
package ch.alpine.ascona.gbc.dn;

import java.util.function.Supplier;

import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.hs.hn.HnFastMean;
import ch.alpine.sophus.hs.hn.HnManifold;
import ch.alpine.sophus.hs.hn.HnPhongMean;
import ch.alpine.tensor.sca.Chop;

/** RMF(p,t,w)[x] == w.t for w = IDC(p,x) */
/* package */ enum HnMeans implements Supplier<BiinvariantMean> {
  EXACT(HnManifold.INSTANCE.biinvariantMean(Chop._05)), //
  FAST(HnFastMean.INSTANCE), //
  PHONG(HnPhongMean.INSTANCE), //
  ;

  private final BiinvariantMean biinvariantMean;

  private HnMeans(BiinvariantMean biinvariantMean) {
    this.biinvariantMean = biinvariantMean;
  }

  @Override
  public BiinvariantMean get() {
    return biinvariantMean;
  }
}
