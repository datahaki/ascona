// code by jph
package ch.alpine.ascona.ref.d1;

import java.util.List;

import ch.alpine.sophis.ref.d1.BSpline4CurveSubdivision;
import ch.alpine.sophis.ref.d1.CurveSubdivision;
import ch.alpine.sophis.ref.d1.FourPointCurveSubdivision;
import ch.alpine.sophus.math.api.GeodesicSpace;

/* package */ enum CurveSubdivisionHelper {
  ;
  public static final List<CurveSubdivisionSchemes> LANE_RIESENFELD = List.of( //
      CurveSubdivisionSchemes.LR1, //
      CurveSubdivisionSchemes.LR2, //
      CurveSubdivisionSchemes.LR3, //
      CurveSubdivisionSchemes.LR4, //
      CurveSubdivisionSchemes.LR5 //
  );

  static CurveSubdivision of(GeodesicSpace geodesicSpace) {
    return BSpline4CurveSubdivision.split3(geodesicSpace, CurveSubdivisionParam.GLOBAL.magicC);
  }

  static CurveSubdivision fps(GeodesicSpace geodesicSpace) {
    return new FourPointCurveSubdivision(geodesicSpace, CurveSubdivisionParam.GLOBAL.omega);
  }
}
