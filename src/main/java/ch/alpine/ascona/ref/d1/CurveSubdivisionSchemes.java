// code by jph
package ch.alpine.ascona.ref.d1;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.ascona.util.dis.R2Display;
import ch.alpine.sophus.hs.HomogeneousSpace;
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
import ch.alpine.tensor.sca.Chop;

/* package */ enum CurveSubdivisionSchemes {
  BSPLINE1(false) {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return new BSpline1CurveSubdivision(manifoldDisplay.geodesicSpace());
    }
  },
  BSPLINE2(true) {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return new BSpline2CurveSubdivision(manifoldDisplay.geodesicSpace());
    }
  },
  BSPLINE3(false) {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return new BSpline3CurveSubdivision(manifoldDisplay.geodesicSpace());
    }
  },
  BSPLINE3LR(false) {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return LaneRiesenfeld3CurveSubdivision.of(manifoldDisplay.geodesicSpace());
    }
  },
  BSPLINE3M(false) {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
      // BiinvariantMean biinvariantMean = manifoldDisplay.biinvariantMean();
      return new MSpline3CurveSubdivision(homogeneousSpace.biinvariantMean(Chop._06));
    }
  },
  /** Dyn/Sharon 2014 that uses 2 binary averages */
  BSPLINE4_S2LO(true) {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return BSpline4CurveSubdivision.split2lo(manifoldDisplay.geodesicSpace());
    }
  },
  /** Alternative to Dyn/Sharon 2014 that also uses 2 binary averages */
  BSPLINE4_S2HI(true) {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return BSpline4CurveSubdivision.split2hi(manifoldDisplay.geodesicSpace());
    }
  },
  /** Hakenberg 2018 that uses 3 binary averages */
  BSPLINE4_S3(true) {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return CurveSubdivisionHelper.of(manifoldDisplay.geodesicSpace());
    }
  },
  /** Hakenberg 2018 that uses 3 binary averages */
  BSPLINE4M(true) {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
      return MSpline4CurveSubdivision.of(homogeneousSpace.biinvariantMean(Chop._06));
    }
  },
  BSPLINE5(false) {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return new BSpline5CurveSubdivision(manifoldDisplay.geodesicSpace());
    }
  },
  BSPLINE6(true) {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return BSpline6CurveSubdivision.of(manifoldDisplay.geodesicSpace());
    }
  },
  LR1(false) {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return LaneRiesenfeldCurveSubdivision.of(manifoldDisplay.geodesicSpace(), 1);
    }
  },
  LR2(true) {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return LaneRiesenfeldCurveSubdivision.of(manifoldDisplay.geodesicSpace(), 2);
    }
  },
  LR3(false) {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return LaneRiesenfeldCurveSubdivision.of(manifoldDisplay.geodesicSpace(), 3);
    }
  },
  LR4(true) {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return LaneRiesenfeldCurveSubdivision.of(manifoldDisplay.geodesicSpace(), 4);
    }
  },
  LR5(false) {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return LaneRiesenfeldCurveSubdivision.of(manifoldDisplay.geodesicSpace(), 5);
    }
  },
  LR6(true) {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return LaneRiesenfeldCurveSubdivision.of(manifoldDisplay.geodesicSpace(), 6);
    }
  },
  DODGSON_SABIN(false) {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return DodgsonSabinCurveSubdivision.INSTANCE;
    }
  },
  THREEPOINT(false) {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return HormannSabinCurveSubdivision.of(manifoldDisplay.geodesicSpace());
    }
  },
  FOURPOINT(true) {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return CurveSubdivisionHelper.fps(manifoldDisplay.geodesicSpace());
    }
  },
  C2CUBIC(true) {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return DualC2FourPointCurveSubdivision.cubic(manifoldDisplay.geodesicSpace());
    }
  },
  C2TIGHT(true) {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return DualC2FourPointCurveSubdivision.tightest(manifoldDisplay.geodesicSpace());
    }
  },
  SIXPOINT(true) {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return new SixPointCurveSubdivision(manifoldDisplay.geodesicSpace());
    }
  },
  SIXFAR(true) {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return new FarSixPointCurveSubdivision(manifoldDisplay.geodesicSpace());
    }
  },
  EIGHTPOINT(true) {
    @Override
    public CurveSubdivision of(ManifoldDisplay manifoldDisplay) {
      return new EightPointCurveSubdivision(manifoldDisplay.geodesicSpace());
    }
  };

  private final boolean isDual;

  CurveSubdivisionSchemes(boolean isDual) {
    this.isDual = isDual;
  }

  public boolean isDual() {
    return isDual;
  }

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
