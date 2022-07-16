// code by jph
package ch.alpine.ascona.util.api;

import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.ref.d1h.Hermite1Subdivisions;
import ch.alpine.sophus.ref.d1h.Hermite2Subdivisions;
import ch.alpine.sophus.ref.d1h.Hermite3Subdivisions;
import ch.alpine.sophus.ref.d1h.HermiteSubdivision;
import ch.alpine.tensor.sca.Chop;

// TODO ASCONA all demos that use this should provide means to modify lambda, mu etc
public enum HermiteSubdivisions {
  HERMITE1 {
    @Override
    public HermiteSubdivision supply(HomogeneousSpace homogeneousSpace, Chop chop) {
      return Hermite1Subdivisions.of(homogeneousSpace, HermiteSubdivisionParam.GLOBAL.hermiteLoParam);
    }
  },
  H1STANDARD {
    @Override
    public HermiteSubdivision supply(HomogeneousSpace homogeneousSpace, Chop chop) {
      return Hermite1Subdivisions.standard(homogeneousSpace);
    }
  },
  // ---
  HERMITE2 {
    @Override
    public HermiteSubdivision supply(HomogeneousSpace homogeneousSpace, Chop chop) {
      return Hermite2Subdivisions.of(homogeneousSpace, HermiteSubdivisionParam.GLOBAL.hermiteLoParam);
    }
  },
  H2STANDARD {
    @Override
    public HermiteSubdivision supply(HomogeneousSpace homogeneousSpace, Chop chop) {
      return Hermite2Subdivisions.standard(homogeneousSpace);
    }
  },
  H2MANIFOLD {
    @Override
    public HermiteSubdivision supply(HomogeneousSpace homogeneousSpace, Chop chop) {
      return Hermite2Subdivisions.manifold(homogeneousSpace);
    }
  },
  // ---
  HERMITE3 {
    @Override
    public HermiteSubdivision supply(HomogeneousSpace homogeneousSpace, Chop chop) {
      return Hermite3Subdivisions.of(homogeneousSpace, chop, HermiteSubdivisionParam.GLOBAL.hermiteHiParam);
    }
  },
  H3STANDARD {
    @Override
    public HermiteSubdivision supply(HomogeneousSpace homogeneousSpace, Chop chop) {
      return Hermite3Subdivisions.of(homogeneousSpace, chop);
    }
  },
  H3A1 {
    @Override
    public HermiteSubdivision supply(HomogeneousSpace homogeneousSpace, Chop chop) {
      return Hermite3Subdivisions.a1(homogeneousSpace, chop);
    }
  },
  H3A2 {
    @Override
    public HermiteSubdivision supply(HomogeneousSpace homogeneousSpace, Chop chop) {
      return Hermite3Subdivisions.a2(homogeneousSpace, chop);
    }
  };

  /** @param homogeneousSpace
   * @param chop
   * @return
   * @throws Exception if either input parameter is null */
  public abstract HermiteSubdivision supply(HomogeneousSpace homogeneousSpace, Chop chop);

  public HermiteSubdivision supply(HomogeneousSpace homogeneousSpace) {
    return supply(homogeneousSpace, Chop._08);
  }
}
