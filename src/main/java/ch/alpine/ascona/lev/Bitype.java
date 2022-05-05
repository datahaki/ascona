// code by jph
package ch.alpine.ascona.lev;

import java.util.Optional;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.sophus.hs.Biinvariant;
import ch.alpine.sophus.hs.Biinvariants;

/* package */ enum Bitype {
  METRIC1, //
  METRIC2, //
  LEVERAGES1, //
  LEVERAGES2, //
  GARDEN, //
  HARBOR, //
  CUPOLA, //
  ;

  public Biinvariant from(ManifoldDisplay manifoldDisplay) {
    return switch (this) {
    case METRIC1, METRIC2 -> Optional.ofNullable(manifoldDisplay.metricBiinvariant()) //
        .orElse(Biinvariants.LEVERAGES);
    case LEVERAGES1, LEVERAGES2 -> Biinvariants.LEVERAGES;
    case GARDEN -> Biinvariants.GARDEN;
    case HARBOR -> Biinvariants.HARBOR;
    case CUPOLA -> Biinvariants.CUPOLA;
    default -> throw new IllegalArgumentException();
    };
  }
}
