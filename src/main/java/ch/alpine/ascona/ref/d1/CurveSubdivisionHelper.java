// code by jph
package ch.alpine.ascona.ref.d1;

import ch.alpine.sophis.ref.d1.BSpline4CurveSubdivision;
import ch.alpine.sophis.ref.d1.CurveSubdivision;
import ch.alpine.sophis.ref.d1.FourPointCurveSubdivision;
import ch.alpine.sophus.api.GeodesicSpace;

/* package */ enum CurveSubdivisionHelper {
  ;
  static CurveSubdivision of(GeodesicSpace geodesicSpace) {
    return BSpline4CurveSubdivision.split3(geodesicSpace, CurveSubdivisionParam.GLOBAL.magicC);
  }

  static CurveSubdivision fps(GeodesicSpace geodesicSpace) {
    return new FourPointCurveSubdivision(geodesicSpace, CurveSubdivisionParam.GLOBAL.omega);
  }
}
