// code by jph
package ch.alpine.ascona.crv.sub;

import ch.alpine.sophis.api.CurveOperator;
import ch.alpine.sophis.ref.d1.BSpline4CurveSubdivision;
import ch.alpine.sophis.ref.d1.FourPointCurveSubdivision;
import ch.alpine.sophus.api.GeodesicSpace;

enum CurveSubdivisionHelper {
  ;
  static CurveOperator of(GeodesicSpace geodesicSpace) {
    return BSpline4CurveSubdivision.split3(geodesicSpace, CurveSubdivisionParam.GLOBAL.magicC);
  }

  static CurveOperator fps(GeodesicSpace geodesicSpace) {
    return new FourPointCurveSubdivision(geodesicSpace, CurveSubdivisionParam.GLOBAL.omega);
  }
}
