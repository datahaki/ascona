// code by ob, jph
package ch.alpine.ascona.util.api;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.flt.bm.BiinvariantMeanCenter;
import ch.alpine.sophus.flt.ga.GeodesicCenter;
import ch.alpine.sophus.flt.ga.GeodesicCenterMidSeeded;
import ch.alpine.sophus.hs.GeodesicSpace;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorUnaryOperator;
import ch.alpine.tensor.sca.Chop;

/** in the current implementation all filters have the same performance for an arbitrary radius */
public enum GeodesicFilters {
  GEODESIC {
    @Override
    public TensorUnaryOperator supply( //
        GeodesicSpace geodesicSpace, ScalarUnaryOperator smoothingKernel, BiinvariantMean biinvariantMean) {
      return GeodesicCenter.of(geodesicSpace, smoothingKernel);
    }
  },
  GEODESIC_MID {
    @Override
    public TensorUnaryOperator supply( //
        GeodesicSpace geodesicSpace, ScalarUnaryOperator smoothingKernel, BiinvariantMean biinvariantMean) {
      return GeodesicCenterMidSeeded.of(geodesicSpace, smoothingKernel);
    }
  },
  BIINVARIANT_MEAN {
    @Override
    public TensorUnaryOperator supply( //
        GeodesicSpace geodesicSpace, ScalarUnaryOperator smoothingKernel, BiinvariantMean biinvariantMean) {
      return BiinvariantMeanCenter.of(biinvariantMean, smoothingKernel);
    }
  };

  /** @param geodesicSpace
   * @param smoothingKernel
   * @param biinvariantMean
   * @return */
  public abstract TensorUnaryOperator supply( //
      GeodesicSpace geodesicSpace, ScalarUnaryOperator smoothingKernel, BiinvariantMean biinvariantMean);

  /** @param manifoldDisplay
   * @param smoothingKernel
   * @return */
  public TensorUnaryOperator from(ManifoldDisplay manifoldDisplay, ScalarUnaryOperator smoothingKernel) {
    HomogeneousSpace homogeneousSpace = (HomogeneousSpace) manifoldDisplay.geodesicSpace();
    BiinvariantMean biinvariantMean = homogeneousSpace.biinvariantMean(Chop._08);
    return supply(homogeneousSpace, smoothingKernel, biinvariantMean);
  }
}