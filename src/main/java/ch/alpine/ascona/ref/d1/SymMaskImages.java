// code by jph
package ch.alpine.ascona.ref.d1;

import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.function.Function;

import ch.alpine.ascona.util.sym.SymGeodesic;
import ch.alpine.ascona.util.sym.SymLinkImage;
import ch.alpine.ascona.util.sym.SymSequence;
import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.sophus.ref.d1.BSpline1CurveSubdivision;
import ch.alpine.sophus.ref.d1.BSpline2CurveSubdivision;
import ch.alpine.sophus.ref.d1.BSpline3CurveSubdivision;
import ch.alpine.sophus.ref.d1.BSpline4CurveSubdivision;
import ch.alpine.sophus.ref.d1.BSpline5CurveSubdivision;
import ch.alpine.sophus.ref.d1.CurveSubdivision;
import ch.alpine.sophus.ref.d1.DualC2FourPointCurveSubdivision;
import ch.alpine.sophus.ref.d1.HormannSabinCurveSubdivision;
import ch.alpine.sophus.ref.d1.LaneRiesenfeld3CurveSubdivision;
import ch.alpine.sophus.ref.d1.LaneRiesenfeldCurveSubdivision;
import ch.alpine.sophus.ref.d1.SixPointCurveSubdivision;
import ch.alpine.tensor.Tensor;

/* package */ enum SymMaskImages {
  BSPLINE1(BSpline1CurveSubdivision::new, 2, 0, 1), //
  BSPLINE2(BSpline2CurveSubdivision::new, 2, 0, 1), //
  BSPLINE3(BSpline3CurveSubdivision::new, 3, 1, 2), //
  BSPLINE3LR(LaneRiesenfeld3CurveSubdivision::of, 3, 1, 2), //
  BSPLINE4(CurveSubdivisionHelper::of, 3, 2, 3), //
  BSPLINE4DS(BSpline4CurveSubdivision::split2lo, 3, 2, 3), //
  BSPLINE4S2(BSpline4CurveSubdivision::split2hi, 3, 2, 3), //
  BSPLINE5(BSpline5CurveSubdivision::new, 4, 2, 3), //
  LR1(gi -> LaneRiesenfeldCurveSubdivision.of(gi, 1), 2, 0, 1), //
  LR2(gi -> LaneRiesenfeldCurveSubdivision.of(gi, 2), 3, 0, 1), //
  LR3(gi -> LaneRiesenfeldCurveSubdivision.of(gi, 3), 3, 1, 2), //
  LR4(gi -> LaneRiesenfeldCurveSubdivision.of(gi, 4), 5, 1, 2), //
  LR5(gi -> LaneRiesenfeldCurveSubdivision.of(gi, 5), 5, 2, 3), //
  LR6(gi -> LaneRiesenfeldCurveSubdivision.of(gi, 6), 5, 2, 3), //
  THREEPOINT(HormannSabinCurveSubdivision::of, 5, 1, 2), //
  FOURPOINT(CurveSubdivisionHelper::fps, 6, 0, 3), //
  C2CUBIC(DualC2FourPointCurveSubdivision::cubic, 6, 2, 3), //
  SIXPOINT(SixPointCurveSubdivision::new, 6, 0, 5);

  private final Function<GeodesicSpace, CurveSubdivision> function;
  private final int support;
  private final int index0;
  private final int index1;

  SymMaskImages(Function<GeodesicSpace, CurveSubdivision> function, int support, int index0, int index1) {
    this.function = function;
    this.support = support;
    this.index0 = index0;
    this.index1 = index1;
  }

  private BufferedImage bufferedImage(int index) {
    CurveSubdivision curveSubdivision = function.apply(SymGeodesic.INSTANCE);
    Tensor vector = SymSequence.of(support);
    Tensor tensor = curveSubdivision.cyclic(vector);
    return new SymLinkImage(tensor.Get(index)).bufferedImage();
  }

  public BufferedImage image0() {
    return bufferedImage(index0);
  }

  public BufferedImage image1() {
    return bufferedImage(index1);
  }

  // ---
  public static Optional<SymMaskImages> get(String string) {
    try {
      return Optional.ofNullable(SymMaskImages.valueOf(string));
    } catch (Exception exception) {
      // ---
    }
    return Optional.empty();
  }
}
