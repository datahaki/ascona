// code by ob, jph
package ch.alpine.ascona.util.api;

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
    public TensorUnaryOperator supply(GeodesicSpace geodesicSpace, ScalarUnaryOperator smoothingKernel) {
      return GeodesicCenter.of(geodesicSpace, smoothingKernel);
    }
  },
  GEODESIC_MID {
    @Override
    public TensorUnaryOperator supply(GeodesicSpace geodesicSpace, ScalarUnaryOperator smoothingKernel) {
      return GeodesicCenterMidSeeded.of(geodesicSpace, smoothingKernel);
    }
  },
  BIINVARIANT_MEAN {
    @Override
    public TensorUnaryOperator supply(GeodesicSpace geodesicSpace, ScalarUnaryOperator smoothingKernel) {
      HomogeneousSpace homogeneousSpace = (HomogeneousSpace) geodesicSpace;
      return BiinvariantMeanCenter.of(homogeneousSpace.biinvariantMean(Chop._08), smoothingKernel);
    }
  };

  /** @param geodesicSpace
   * @param smoothingKernel
   * @return */
  public abstract TensorUnaryOperator supply(GeodesicSpace geodesicSpace, ScalarUnaryOperator smoothingKernel);
}
