// code by jph
package ch.alpine.ascona.ref.d1;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.sophus.ref.d1.BSpline4CurveSubdivision;
import ch.alpine.sophus.ref.d1.CurveSubdivision;
import ch.alpine.sophus.ref.d1.FourPointCurveSubdivision;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.Scalar;

/* package */ enum CurveSubdivisionHelper {
  ;
  public static final List<CurveSubdivisionSchemes> LANE_RIESENFELD = Arrays.asList( //
      CurveSubdivisionSchemes.LR1, //
      CurveSubdivisionSchemes.LR2, //
      CurveSubdivisionSchemes.LR3, //
      CurveSubdivisionSchemes.LR4, //
      CurveSubdivisionSchemes.LR5 //
  );
  // ---
  private static final Set<CurveSubdivisionSchemes> DUAL = EnumSet.of( //
      CurveSubdivisionSchemes.BSPLINE2, //
      CurveSubdivisionSchemes.BSPLINE4_S3, //
      CurveSubdivisionSchemes.BSPLINE4_S2LO, //
      CurveSubdivisionSchemes.BSPLINE4_S2HI, //
      CurveSubdivisionSchemes.BSPLINE4M, //
      CurveSubdivisionSchemes.LR2, //
      CurveSubdivisionSchemes.LR4, //
      CurveSubdivisionSchemes.LR6);

  static boolean isDual(CurveSubdivisionSchemes curveSubdivisionSchemes) {
    return DUAL.contains(curveSubdivisionSchemes);
  }

  // ---
  // TODO ASCONA bad design
  static Scalar MAGIC_C = RationalScalar.of(1, 6);

  static CurveSubdivision of(GeodesicSpace geodesicSpace) {
    return BSpline4CurveSubdivision.split3(geodesicSpace, MAGIC_C);
  }

  static Scalar OMEGA = RationalScalar.of(1, 16);

  static CurveSubdivision fps(GeodesicSpace geodesicSpace) {
    return new FourPointCurveSubdivision(geodesicSpace, OMEGA);
  }
}
