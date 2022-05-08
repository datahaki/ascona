// code by ob, jph
package ch.alpine.ascona.util.api;

import ch.alpine.ascona.util.dis.ManifoldDisplay;
import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.sophus.api.SplitInterface;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.flt.bm.BiinvariantMeanCenter;
import ch.alpine.sophus.flt.ga.GeodesicCenter;
import ch.alpine.sophus.flt.ga.GeodesicCenterMidSeeded;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorUnaryOperator;

/** in the current implementation all filters have the same performance for an arbitrary radius */
public enum GeodesicFilters {
  GEODESIC {
    @Override
    public TensorUnaryOperator supply( //
        SplitInterface splitInterface, ScalarUnaryOperator smoothingKernel, BiinvariantMean biinvariantMean) {
      return GeodesicCenter.of(splitInterface, smoothingKernel);
    }
  },
  GEODESIC_MID {
    @Override
    public TensorUnaryOperator supply( //
        SplitInterface splitInterface, ScalarUnaryOperator smoothingKernel, BiinvariantMean biinvariantMean) {
      return GeodesicCenterMidSeeded.of(splitInterface, smoothingKernel);
    }
  },
  BIINVARIANT_MEAN {
    @Override
    public TensorUnaryOperator supply( //
        SplitInterface splitInterface, ScalarUnaryOperator smoothingKernel, BiinvariantMean biinvariantMean) {
      return BiinvariantMeanCenter.of(biinvariantMean, smoothingKernel);
    }
  };

  /** @param splitInterface
   * @param smoothingKernel
   * @param biinvariantMean
   * @return */
  public abstract TensorUnaryOperator supply( //
      SplitInterface splitInterface, ScalarUnaryOperator smoothingKernel, BiinvariantMean biinvariantMean);

  /** @param manifoldDisplay
   * @param smoothingKernel
   * @return */
  public TensorUnaryOperator from(ManifoldDisplay manifoldDisplay, ScalarUnaryOperator smoothingKernel) {
    GeodesicSpace geodesicSpace = manifoldDisplay.geodesicSpace();
    BiinvariantMean biinvariantMean = manifoldDisplay.biinvariantMean();
    return supply(geodesicSpace, smoothingKernel, biinvariantMean);
  }
}