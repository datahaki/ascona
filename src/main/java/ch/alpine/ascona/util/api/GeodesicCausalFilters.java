// code by ob, jph
package ch.alpine.ascona.util.api;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.sophus.crv.MonomialExtrapolationMask;
import ch.alpine.sophus.flt.bm.BiinvariantMeanExtrapolation;
import ch.alpine.sophus.flt.ga.GeodesicExtrapolation;
import ch.alpine.sophus.flt.ga.GeodesicFIRn;
import ch.alpine.sophus.flt.ga.GeodesicIIRn;
import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.sca.Chop;

public enum GeodesicCausalFilters {
  GEODESIC_FIR {
    @Override
    public TensorUnaryOperator supply( //
        ManifoldDisplay manifoldDisplay, ScalarUnaryOperator smoothingKernel, int radius, Scalar alpha) {
      GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
      TensorUnaryOperator geodesicExtrapolation = GeodesicExtrapolation.of(geodesicSpace, smoothingKernel);
      return GeodesicIIRn.of(geodesicExtrapolation, geodesicSpace, radius, alpha);
    }
  },
  GEODESIC_IIR {
    @Override
    public TensorUnaryOperator supply( //
        ManifoldDisplay manifoldDisplay, ScalarUnaryOperator smoothingKernel, int radius, Scalar alpha) {
      GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
      TensorUnaryOperator geodesicExtrapolation = GeodesicExtrapolation.of(geodesicSpace, smoothingKernel);
      return GeodesicFIRn.of(geodesicExtrapolation, geodesicSpace, radius, alpha);
    }
  },
  BIINVARIANT_MEAN_FIR {
    @Override
    public TensorUnaryOperator supply( //
        ManifoldDisplay manifoldDisplay, ScalarUnaryOperator smoothingKernel, int radius, Scalar alpha) {
      HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
      TensorUnaryOperator geodesicExtrapolation = new BiinvariantMeanExtrapolation( //
          homogeneousSpace.biinvariantMean(Chop._08), MonomialExtrapolationMask.INSTANCE);
      return GeodesicFIRn.of(geodesicExtrapolation, manifoldDisplay.geodesicSpace(), radius, alpha);
    }
  },
  BIINVARIANT_MEAN_IIR {
    @Override
    public TensorUnaryOperator supply( //
        ManifoldDisplay manifoldDisplay, ScalarUnaryOperator smoothingKernel, int radius, Scalar alpha) {
      HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
      TensorUnaryOperator geodesicExtrapolation = new BiinvariantMeanExtrapolation( //
          homogeneousSpace.biinvariantMean(Chop._08), MonomialExtrapolationMask.INSTANCE);
      return GeodesicIIRn.of(geodesicExtrapolation, manifoldDisplay.geodesicSpace(), radius, alpha);
    }
  };

  public abstract TensorUnaryOperator supply( //
      ManifoldDisplay manifoldDisplay, ScalarUnaryOperator smoothingKernel, int radius, Scalar alpha);
}