// code by jph
package ch.alpine.ascona.ref.d1;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.R2Display;
import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.sophus.api.MidpointInterface;
import ch.alpine.sophus.api.SplitInterface;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.ref.d1.BSpline1CurveSubdivision;
import ch.alpine.sophus.ref.d1.BSpline2CurveSubdivision;
import ch.alpine.sophus.ref.d1.BSpline3CurveSubdivision;
import ch.alpine.sophus.ref.d1.BSpline4CurveSubdivision;
import ch.alpine.sophus.ref.d1.BSpline5CurveSubdivision;
import ch.alpine.sophus.ref.d1.BSpline6CurveSubdivision;
import ch.alpine.sophus.ref.d1.CurveSubdivision;
import ch.alpine.sophus.ref.d1.DodgsonSabinCurveSubdivision;
import ch.alpine.sophus.ref.d1.DualC2FourPointCurveSubdivision;
import ch.alpine.sophus.ref.d1.EightPointCurveSubdivision;
import ch.alpine.sophus.ref.d1.FarSixPointCurveSubdivision;
import ch.alpine.sophus.ref.d1.HormannSabinCurveSubdivision;
import ch.alpine.sophus.ref.d1.LaneRiesenfeld3CurveSubdivision;
import ch.alpine.sophus.ref.d1.LaneRiesenfeldCurveSubdivision;
import ch.alpine.sophus.ref.d1.MSpline3CurveSubdivision;
import ch.alpine.sophus.ref.d1.MSpline4CurveSubdivision;
import ch.alpine.sophus.ref.d1.SixPointCurveSubdivision;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.itp.BinaryAverage;

/* package */ enum CurveSubdivisionSchemes {
  BSPLINE1 {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      MidpointInterface midpointInterface = manifoldDisplay.geodesic();
      return new BSpline1CurveSubdivision(midpointInterface);
    }
  },
  BSPLINE2 {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      GeodesicSpace parametricCurve = manifoldDisplay.geodesic();
      return new BSpline2CurveSubdivision(parametricCurve);
    }
  },
  BSPLINE3 {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      SplitInterface splitInterface = manifoldDisplay.geodesic();
      return new BSpline3CurveSubdivision(splitInterface);
    }
  },
  BSPLINE3LR {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      MidpointInterface midpointInterface = manifoldDisplay.geodesic();
      return LaneRiesenfeld3CurveSubdivision.of(midpointInterface);
    }
  },
  BSPLINE3M {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      BiinvariantMean biinvariantMean = manifoldDisplay.biinvariantMean();
      return new MSpline3CurveSubdivision(biinvariantMean);
    }
  },
  /** Dyn/Sharon 2014 that uses 2 binary averages */
  BSPLINE4_S2LO {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      SplitInterface splitInterface = manifoldDisplay.geodesic();
      return BSpline4CurveSubdivision.split2lo(splitInterface);
    }
  },
  /** Alternative to Dyn/Sharon 2014 that also uses 2 binary averages */
  BSPLINE4_S2HI {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      SplitInterface splitInterface = manifoldDisplay.geodesic();
      return BSpline4CurveSubdivision.split2hi(splitInterface);
    }
  },
  /** Hakenberg 2018 that uses 3 binary averages */
  BSPLINE4_S3 {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      SplitInterface splitInterface = manifoldDisplay.geodesic();
      return CurveSubdivisionHelper.of(splitInterface);
    }
  },
  /** Hakenberg 2018 that uses 3 binary averages */
  BSPLINE4M {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      BiinvariantMean biinvariantMean = manifoldDisplay.biinvariantMean();
      return MSpline4CurveSubdivision.of(biinvariantMean);
    }
  },
  BSPLINE5 {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      SplitInterface splitInterface = manifoldDisplay.geodesic();
      return new BSpline5CurveSubdivision(splitInterface);
    }
  },
  BSPLINE6 {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      GeodesicSpace geodesicInterface = manifoldDisplay.geodesic();
      return BSpline6CurveSubdivision.of(geodesicInterface);
    }
  },
  LR1 {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      MidpointInterface midpointInterface = manifoldDisplay.geodesic();
      return LaneRiesenfeldCurveSubdivision.of(midpointInterface, 1);
    }
  },
  LR2 {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      MidpointInterface midpointInterface = manifoldDisplay.geodesic();
      return LaneRiesenfeldCurveSubdivision.of(midpointInterface, 2);
    }
  },
  LR3 {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      MidpointInterface midpointInterface = manifoldDisplay.geodesic();
      return LaneRiesenfeldCurveSubdivision.of(midpointInterface, 3);
    }
  },
  LR4 {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      MidpointInterface midpointInterface = manifoldDisplay.geodesic();
      return LaneRiesenfeldCurveSubdivision.of(midpointInterface, 4);
    }
  },
  LR5 {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      MidpointInterface midpointInterface = manifoldDisplay.geodesic();
      return LaneRiesenfeldCurveSubdivision.of(midpointInterface, 5);
    }
  },
  LR6 {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      MidpointInterface midpointInterface = manifoldDisplay.geodesic();
      return LaneRiesenfeldCurveSubdivision.of(midpointInterface, 6);
    }
  },
  DODGSON_SABIN {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return DodgsonSabinCurveSubdivision.INSTANCE;
    }
  },
  THREEPOINT {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      BinaryAverage binaryAverage = manifoldDisplay.geodesic();
      return HormannSabinCurveSubdivision.of(binaryAverage);
    }
  },
  FOURPOINT {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      SplitInterface splitInterface = manifoldDisplay.geodesic();
      return CurveSubdivisionHelper.fps(splitInterface);
    }
  },
  C2CUBIC {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      GeodesicSpace geodesicInterface = manifoldDisplay.geodesic();
      return DualC2FourPointCurveSubdivision.cubic(geodesicInterface);
    }
  },
  C2TIGHT {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      GeodesicSpace geodesicInterface = manifoldDisplay.geodesic();
      return DualC2FourPointCurveSubdivision.tightest(geodesicInterface);
    }
  },
  SIXPOINT {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      SplitInterface splitInterface = manifoldDisplay.geodesic();
      return new SixPointCurveSubdivision(splitInterface);
    }
  },
  SIXFAR {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      SplitInterface splitInterface = manifoldDisplay.geodesic();
      return new FarSixPointCurveSubdivision(splitInterface);
    }
  },
  EIGHTPOINT {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      SplitInterface splitInterface = manifoldDisplay.geodesic();
      return new EightPointCurveSubdivision(splitInterface);
    }
  };

  public abstract CurveSubdivision of(ManifoldDisplay manifoldDisplay);

  public boolean isStringSupported() {
    try {
      of(R2Display.INSTANCE).string(Tensors.empty());
      return true;
    } catch (Exception exception) {
      // ---
    }
    return false;
  }
}
