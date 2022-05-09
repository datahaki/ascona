// code by jph
package ch.alpine.ascona.ref.d1;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.R2Display;
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

/* package */ enum CurveSubdivisionSchemes {
  BSPLINE1 {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return new BSpline1CurveSubdivision(manifoldDisplay.geodesicSpace());
    }
  },
  BSPLINE2 {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return new BSpline2CurveSubdivision(manifoldDisplay.geodesicSpace());
    }
  },
  BSPLINE3 {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return new BSpline3CurveSubdivision(manifoldDisplay.geodesicSpace());
    }
  },
  BSPLINE3LR {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return LaneRiesenfeld3CurveSubdivision.of(manifoldDisplay.geodesicSpace());
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
      return BSpline4CurveSubdivision.split2lo(manifoldDisplay.geodesicSpace());
    }
  },
  /** Alternative to Dyn/Sharon 2014 that also uses 2 binary averages */
  BSPLINE4_S2HI {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return BSpline4CurveSubdivision.split2hi(manifoldDisplay.geodesicSpace());
    }
  },
  /** Hakenberg 2018 that uses 3 binary averages */
  BSPLINE4_S3 {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return CurveSubdivisionHelper.of(manifoldDisplay.geodesicSpace());
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
      return new BSpline5CurveSubdivision(manifoldDisplay.geodesicSpace());
    }
  },
  BSPLINE6 {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return BSpline6CurveSubdivision.of(manifoldDisplay.geodesicSpace());
    }
  },
  LR1 {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return LaneRiesenfeldCurveSubdivision.of(manifoldDisplay.geodesicSpace(), 1);
    }
  },
  LR2 {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return LaneRiesenfeldCurveSubdivision.of(manifoldDisplay.geodesicSpace(), 2);
    }
  },
  LR3 {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return LaneRiesenfeldCurveSubdivision.of(manifoldDisplay.geodesicSpace(), 3);
    }
  },
  LR4 {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return LaneRiesenfeldCurveSubdivision.of(manifoldDisplay.geodesicSpace(), 4);
    }
  },
  LR5 {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return LaneRiesenfeldCurveSubdivision.of(manifoldDisplay.geodesicSpace(), 5);
    }
  },
  LR6 {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return LaneRiesenfeldCurveSubdivision.of(manifoldDisplay.geodesicSpace(), 6);
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
      return HormannSabinCurveSubdivision.of(manifoldDisplay.geodesicSpace());
    }
  },
  FOURPOINT {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return CurveSubdivisionHelper.fps(manifoldDisplay.geodesicSpace());
    }
  },
  C2CUBIC {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return DualC2FourPointCurveSubdivision.cubic(manifoldDisplay.geodesicSpace());
    }
  },
  C2TIGHT {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return DualC2FourPointCurveSubdivision.tightest(manifoldDisplay.geodesicSpace());
    }
  },
  SIXPOINT {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return new SixPointCurveSubdivision(manifoldDisplay.geodesicSpace());
    }
  },
  SIXFAR {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return new FarSixPointCurveSubdivision(manifoldDisplay.geodesicSpace());
    }
  },
  EIGHTPOINT {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return new EightPointCurveSubdivision(manifoldDisplay.geodesicSpace());
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
