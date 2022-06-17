// code by jph
package ch.alpine.ascona.lev;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.sophus.hs.Biinvariant;
import ch.alpine.sophus.hs.Biinvariants;
import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.sophus.hs.MetricManifold;

public enum Bitype {
  METRIC1, //
  METRIC2, //
  LEVERAGES1, //
  LEVERAGES2, //
  GARDEN, //
  HARBOR, //
  CUPOLA, //
  ;

  public Biinvariant from(ManifoldDisplay manifoldDisplay) {
    GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
    if (equals(METRIC1) || equals(METRIC2))
      if (!(geodesicSpace instanceof MetricManifold))
        return Biinvariants.LEVERAGES;
    return switch (this) {
    case METRIC1, METRIC2 -> ((MetricManifold) geodesicSpace).biinvariant();
    case LEVERAGES1, LEVERAGES2 -> Biinvariants.LEVERAGES;
    case GARDEN -> Biinvariants.GARDEN;
    case HARBOR -> Biinvariants.HARBOR;
    case CUPOLA -> Biinvariants.CUPOLA;
    default -> throw new IllegalArgumentException();
    };
  }
}
