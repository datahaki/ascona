// code by jph
package ch.alpine.ascona.ref;

import java.util.List;

import ch.alpine.bridge.ref.ann.FieldSelectionCallback;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.dv.Biinvariant;
import ch.alpine.sophis.dv.Biinvariants;
import ch.alpine.sophus.api.Manifold;

@ReflectionMarker
public final class BiinvariantsParam {
  public static BiinvariantsParam fast() {
    return new BiinvariantsParam(Biinvariants.FAST);
  }

  public static BiinvariantsParam okay() {
    return new BiinvariantsParam(Biinvariants.OKAY);
  }

  // ---
  private final List<Biinvariants> list;

  public BiinvariantsParam(List<Biinvariants> list) {
    this.list = list;
  }

  @FieldSelectionCallback("biinvariants")
  public Biinvariants biinvariants = Biinvariants.METRIC;

  public Biinvariant ofSafe(Manifold manifold) {
    return biinvariants.ofSafe(manifold);
  }

  @ReflectionMarker
  public List<Biinvariants> biinvariants() {
    return list;
  }
}
