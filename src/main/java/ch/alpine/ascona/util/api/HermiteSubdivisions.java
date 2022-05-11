// code by jph
package ch.alpine.ascona.util.api;

import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.sophus.ref.d1h.Hermite1Subdivisions;
import ch.alpine.sophus.ref.d1h.Hermite2Subdivisions;
import ch.alpine.sophus.ref.d1h.Hermite3Subdivisions;
import ch.alpine.sophus.ref.d1h.HermiteSubdivision;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.sca.Chop;

// TODO ASCONA all demos that use this should provide means to modify lambda, mu etc
public enum HermiteSubdivisions {
  HERMITE1 {
    @Override
    public HermiteSubdivision supply(HomogeneousSpace hsManifold, Chop chop) {
      return Hermite1Subdivisions.of(hsManifold, LAMBDA, MU);
    }
  },
  H1STANDARD {
    @Override
    public HermiteSubdivision supply(HomogeneousSpace hsManifold, Chop chop) {
      return Hermite1Subdivisions.standard(hsManifold);
    }
  },
  // ---
  HERMITE2 {
    @Override
    public HermiteSubdivision supply(HomogeneousSpace hsManifold, Chop chop) {
      return Hermite2Subdivisions.of(hsManifold, LAMBDA, MU);
    }
  },
  H2STANDARD {
    @Override
    public HermiteSubdivision supply(HomogeneousSpace hsManifold, Chop chop) {
      return Hermite2Subdivisions.standard(hsManifold);
    }
  },
  H2MANIFOLD {
    @Override
    public HermiteSubdivision supply(HomogeneousSpace hsManifold, Chop chop) {
      return Hermite2Subdivisions.manifold(hsManifold);
    }
  },
  // ---
  HERMITE3 {
    @Override
    public HermiteSubdivision supply(HomogeneousSpace hsManifold, Chop chop) {
      return Hermite3Subdivisions.of(hsManifold, THETA, OMEGA);
    }
  },
  H3STANDARD {
    @Override
    public HermiteSubdivision supply(HomogeneousSpace hsManifold, Chop chop) {
      return Hermite3Subdivisions.of(hsManifold);
    }
  },
  H3A1 {
    @Override
    public HermiteSubdivision supply(HomogeneousSpace hsManifold, Chop chop) {
      return Hermite3Subdivisions.a1(hsManifold);
    }
  },
  H3A2 {
    @Override
    public HermiteSubdivision supply(HomogeneousSpace hsManifold, Chop chop) {
      return Hermite3Subdivisions.a2(hsManifold);
    }
  },
  // ---
  HERMITE3_BM {
    @Override
    public HermiteSubdivision supply(HomogeneousSpace hsManifold, Chop chop) {
      return Hermite3Subdivisions.of(hsManifold, hsManifold.biinvariantMean(chop), THETA, OMEGA);
    }
  },
  H3STANDARD_BM {
    @Override
    public HermiteSubdivision supply(HomogeneousSpace hsManifold, Chop chop) {
      return Hermite3Subdivisions.of(hsManifold, hsManifold.biinvariantMean(chop));
    }
  },
  H3A1_BM {
    @Override
    public HermiteSubdivision supply(HomogeneousSpace hsManifold, Chop chop) {
      return Hermite3Subdivisions.a1(hsManifold, hsManifold.biinvariantMean(chop));
    }
  },
  H3A2_BM {
    @Override
    public HermiteSubdivision supply(HomogeneousSpace hsManifold, Chop chop) {
      return Hermite3Subdivisions.a2(hsManifold, hsManifold.biinvariantMean(Chop._08));
    }
  };

  // TODO ASCONA ALG class design is no good
  public static Scalar LAMBDA = RationalScalar.of(-1, 8);
  public static Scalar MU = RationalScalar.of(-1, 2);
  public static Scalar THETA = RationalScalar.of(+1, 128);
  public static Scalar OMEGA = RationalScalar.of(-1, 16);

  /** @param lieGroup
   * @param exponential
   * @param biinvariantMean
   * @return
   * @throws Exception if either input parameter is null */
  public abstract HermiteSubdivision supply( //
      HomogeneousSpace hsManifold, Chop chop);

  public HermiteSubdivision supply(HomogeneousSpace hsManifold) {
    return supply(hsManifold, Chop._08);
  }
}
