// code by ob
package ch.alpine.ascona.flt;

import ch.alpine.ascony.api.GeodesicCausalFilters;
import ch.alpine.bridge.ref.ann.FieldClip;
import ch.alpine.bridge.ref.ann.FieldSlider;
import ch.alpine.bridge.ref.ann.ReflectionMarker;
import ch.alpine.sophis.flt.WindowSideExtrapolation;
import ch.alpine.sophis.flt.bm.BiinvariantMeanFIRnFilter;
import ch.alpine.sophis.flt.bm.BiinvariantMeanIIRnFilter;
import ch.alpine.sophis.flt.ga.GeodesicExtrapolation;
import ch.alpine.sophis.flt.ga.GeodesicFIRnFilter;
import ch.alpine.sophis.flt.ga.GeodesicIIRnFilter;
import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.sophus.bm.BiinvariantMean;
import ch.alpine.sophus.hs.HomogeneousSpace;
import ch.alpine.tensor.Rational;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorUnaryOperator;

class GeodesicCausalFilterDemo extends AbstractSpectrogramDemo {
  @ReflectionMarker
  static class Paraf {
    public GeodesicCausalFilters gcf = GeodesicCausalFilters.BIINVARIANT_MEAN_FIR;
    /** parameter to blend extrapolation with measurement */
    @FieldSlider
    @FieldClip(min = "0", max = "1")
    public Scalar value = Rational.HALF;
  }

  private final Paraf paraf;

  public GeodesicCausalFilterDemo() {
    super(paraf = new Paraf());
    fieldsEditor(0).addUniversalListener(this::updateState);
    // ---
    updateState();
  }

  @Override // from RenderInterface
  protected Tensor process(Tensor control) {
    final int radius = specParam.radius;
    GeodesicSpace geodesicSpace = manifoldDisplay().geodesicSpace();
    if (0 < radius) {
      ScalarUnaryOperator windowFunctions = specParam.kernel.get();
      BiinvariantMean biinvariantMean = geodesicSpace instanceof HomogeneousSpace homogeneousSpace //
          ? homogeneousSpace.biinvariantMean()
          : null;
      ;
      GeodesicCausalFilters geodesicCausalFilters = paraf.gcf.ofSafe(geodesicSpace);
      // TODO ASCONA ALG should be able to do with geodesicCausalFilters.supply, but doesn't
      TensorUnaryOperator geodesicExtrapolation = GeodesicExtrapolation.of(geodesicSpace, windowFunctions);
      TensorUnaryOperator tuo = switch (geodesicCausalFilters) {
      case GEODESIC_FIR -> GeodesicFIRnFilter.of(geodesicExtrapolation, geodesicSpace, radius, alpha());
      case GEODESIC_IIR -> GeodesicIIRnFilter.of(geodesicExtrapolation, geodesicSpace, radius, alpha());
      case BIINVARIANT_MEAN_FIR -> BiinvariantMeanFIRnFilter.of( //
          biinvariantMean, WindowSideExtrapolation.of(windowFunctions), geodesicSpace, radius, alpha());
      case BIINVARIANT_MEAN_IIR -> BiinvariantMeanIIRnFilter.of( //
          biinvariantMean, WindowSideExtrapolation.of(windowFunctions), geodesicSpace, radius, alpha());
      };
      return tuo.apply(control);
    }
    return control;
  }

  private Scalar alpha() {
    return paraf.value;
  }

  @Override
  protected String plotLabel() {
    return super.plotLabel() + " " + alpha();
  }

  static void main() {
    new GeodesicCausalFilterDemo().runStandalone();
  }
}
