// code by jph
package ch.alpine.ascona.crv.sub;

import ch.alpine.ascony.dis.ManifoldDisplay;
import ch.alpine.ascony.dis.R2Display;
import ch.alpine.sophis.api.CurveOperator;
import ch.alpine.sophis.ref.d1.BSpline1CurveSubdivision;
import ch.alpine.sophis.ref.d1.BSpline2CurveSubdivision;
import ch.alpine.sophis.ref.d1.BSpline3CurveSubdivision;
import ch.alpine.sophis.ref.d1.BSpline4CurveSubdivision;
import ch.alpine.sophis.ref.d1.BSpline5CurveSubdivision;
import ch.alpine.sophis.ref.d1.BSpline6CurveSubdivision;
import ch.alpine.sophis.ref.d1.DodgsonSabinCurveSubdivision;
import ch.alpine.sophis.ref.d1.DualC2FourPointCurveSubdivision;
import ch.alpine.sophis.ref.d1.EightPointCurveSubdivision;
import ch.alpine.sophis.ref.d1.FarSixPointCurveSubdivision;
import ch.alpine.sophis.ref.d1.HormannSabinCurveSubdivision;
import ch.alpine.sophis.ref.d1.LaneRiesenfeld3CurveSubdivision;
import ch.alpine.sophis.ref.d1.LaneRiesenfeldCurveSubdivision;
import ch.alpine.sophis.ref.d1.MSpline3CurveSubdivision;
import ch.alpine.sophis.ref.d1.MSpline4CurveSubdivision;
import ch.alpine.sophis.ref.d1.SixPointCurveSubdivision;
import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.Tensors;
import ch.alpine.tensor.alg.Join;
import ch.alpine.tensor.alg.Last;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.ext.Integers;

enum CurveSubdivisionSchemes {
  BSPLINE1(false) {
    @Override
    public CurveOperator of(ManifoldDisplay manifoldDisplay) {
      return new BSpline1CurveSubdivision(manifoldDisplay.geodesicSpace());
    }
  },
  BSPLINE2(true) {
    @Override
    public CurveOperator of(ManifoldDisplay manifoldDisplay) {
      return new BSpline2CurveSubdivision(manifoldDisplay.geodesicSpace());
    }
  },
  BSPLINE3(false) {
    @Override
    public CurveOperator of(ManifoldDisplay manifoldDisplay) {
      return new BSpline3CurveSubdivision(manifoldDisplay.geodesicSpace());
    }
  },
  BSPLINE3LR(false) {
    @Override
    public CurveOperator of(ManifoldDisplay manifoldDisplay) {
      return LaneRiesenfeld3CurveSubdivision.of(manifoldDisplay.geodesicSpace());
    }
  },
  BSPLINE3M(false) {
    @Override
    public CurveOperator of(ManifoldDisplay manifoldDisplay) {
      HomogeneousSpace homogeneousSpace = manifoldDisplay.homogeneousSpace();
      // BiinvariantMean biinvariantMean = manifoldDisplay.biinvariantMean();
      return new MSpline3CurveSubdivision(homogeneousSpace.biinvariantMean());
    }
  },
  /** Dyn/Sharon 2014 that uses 2 binary averages */
  BSPLINE4_S2LO(true) {
    @Override
    public CurveOperator of(ManifoldDisplay manifoldDisplay) {
      return BSpline4CurveSubdivision.split2lo(manifoldDisplay.geodesicSpace());
    }
  },
  /** Alternative to Dyn/Sharon 2014 that also uses 2 binary averages */
  BSPLINE4_S2HI(true) {
    @Override
    public CurveOperator of(ManifoldDisplay manifoldDisplay) {
      return BSpline4CurveSubdivision.split2hi(manifoldDisplay.geodesicSpace());
    }
  },
  /** Hakenberg 2018 that uses 3 binary averages */
  BSPLINE4_S3(true) {
    @Override
    public CurveOperator of(ManifoldDisplay manifoldDisplay) {
      return CurveSubdivisionHelper.of(manifoldDisplay.geodesicSpace());
    }
  },
  /** Hakenberg 2018 that uses 3 binary averages */
  BSPLINE4M(true) {
    @Override
    public CurveOperator of(ManifoldDisplay manifoldDisplay) {
      HomogeneousSpace homogeneousSpace = manifoldDisplay.homogeneousSpace();
      return MSpline4CurveSubdivision.of(homogeneousSpace.biinvariantMean());
    }
  },
  BSPLINE5(false) {
    @Override
    public CurveOperator of(ManifoldDisplay manifoldDisplay) {
      return new BSpline5CurveSubdivision(manifoldDisplay.geodesicSpace());
    }
  },
  BSPLINE6(true) {
    @Override
    public CurveOperator of(ManifoldDisplay manifoldDisplay) {
      return BSpline6CurveSubdivision.of(manifoldDisplay.geodesicSpace());
    }
  },
  LR1(false) {
    @Override
    public CurveOperator of(ManifoldDisplay manifoldDisplay) {
      return LaneRiesenfeldCurveSubdivision.of(manifoldDisplay.geodesicSpace(), 1);
    }
  },
  LR2(true) {
    @Override
    public CurveOperator of(ManifoldDisplay manifoldDisplay) {
      return LaneRiesenfeldCurveSubdivision.of(manifoldDisplay.geodesicSpace(), 2);
    }
  },
  LR3(false) {
    @Override
    public CurveOperator of(ManifoldDisplay manifoldDisplay) {
      return LaneRiesenfeldCurveSubdivision.of(manifoldDisplay.geodesicSpace(), 3);
    }
  },
  LR4(true) {
    @Override
    public CurveOperator of(ManifoldDisplay manifoldDisplay) {
      return LaneRiesenfeldCurveSubdivision.of(manifoldDisplay.geodesicSpace(), 4);
    }
  },
  LR5(false) {
    @Override
    public CurveOperator of(ManifoldDisplay manifoldDisplay) {
      return LaneRiesenfeldCurveSubdivision.of(manifoldDisplay.geodesicSpace(), 5);
    }
  },
  LR6(true) {
    @Override
    public CurveOperator of(ManifoldDisplay manifoldDisplay) {
      return LaneRiesenfeldCurveSubdivision.of(manifoldDisplay.geodesicSpace(), 6);
    }
  },
  DODGSON_SABIN(false) {
    @Override
    public CurveOperator of(ManifoldDisplay manifoldDisplay) {
      return DodgsonSabinCurveSubdivision.INSTANCE;
    }
  },
  THREEPOINT(false) {
    @Override
    public CurveOperator of(ManifoldDisplay manifoldDisplay) {
      return HormannSabinCurveSubdivision.of(manifoldDisplay.geodesicSpace());
    }
  },
  FOURPOINT(true) {
    @Override
    public CurveOperator of(ManifoldDisplay manifoldDisplay) {
      return CurveSubdivisionHelper.fps(manifoldDisplay.geodesicSpace());
    }
  },
  C2CUBIC(true) {
    @Override
    public CurveOperator of(ManifoldDisplay manifoldDisplay) {
      return DualC2FourPointCurveSubdivision.cubic(manifoldDisplay.geodesicSpace());
    }
  },
  C2TIGHT(true) {
    @Override
    public CurveOperator of(ManifoldDisplay manifoldDisplay) {
      return DualC2FourPointCurveSubdivision.tightest(manifoldDisplay.geodesicSpace());
    }
  },
  SIXPOINT(true) {
    @Override
    public CurveOperator of(ManifoldDisplay manifoldDisplay) {
      return new SixPointCurveSubdivision(manifoldDisplay.geodesicSpace());
    }
  },
  SIXFAR(true) {
    @Override
    public CurveOperator of(ManifoldDisplay manifoldDisplay) {
      return new FarSixPointCurveSubdivision(manifoldDisplay.geodesicSpace());
    }
  },
  EIGHTPOINT(true) {
    @Override
    public CurveOperator of(ManifoldDisplay manifoldDisplay) {
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

  public abstract CurveOperator of(ManifoldDisplay manifoldDisplay);

  public boolean isStringSupported() {
    try {
      of(R2Display.INSTANCE).string(Tensors.empty());
      return true;
    } catch (Exception exception) {
      // ---
    }
    return false;
  }

  public Tensor refine(ManifoldDisplay manifoldDisplay, Tensor control, int levels, boolean cyclic) {
    GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
    CurveOperator curveOperator = of(manifoldDisplay);
    TensorUnaryOperator tensorUnaryOperator = curveOperator.auto(cyclic);
    Tensor refined = control;
    for (int level = 0; level < levels; ++level) {
      Tensor prev = refined;
      refined = tensorUnaryOperator.apply(refined);
      if (isDual && //
          Integers.isOdd(level) && //
          !cyclic && //
          1 < control.length())
        refined = Join.of( //
            Tensors.of(geodesicSpace.midpoint(control.get(0), prev.get(0))), //
            refined, //
            Tensors.of(geodesicSpace.midpoint(Last.of(prev), Last.of(control))));
    }
    return refined;
  }
}
