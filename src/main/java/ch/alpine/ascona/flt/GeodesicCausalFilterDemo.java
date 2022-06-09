// code by ob
package ch.alpine.ascona.flt;

import java.awt.Dimension;
import java.awt.Graphics2D;

import javax.swing.JSlider;

import ch.alpine.ascona.util.api.GeodesicCausalFilters;
import ch.alpine.ascona.util.dat.GokartPoseDataV2;
import ch.alpine.ascona.util.dis.ManifoldDisplays;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.swing.SpinnerLabel;
import ch.alpine.sophus.api.GeodesicSpace;
import ch.alpine.sophus.flt.WindowSideExtrapolation;
import ch.alpine.sophus.flt.bm.BiinvariantMeanFIRnFilter;
import ch.alpine.sophus.flt.bm.BiinvariantMeanIIRnFilter;
import ch.alpine.sophus.flt.ga.GeodesicExtrapolation;
import ch.alpine.sophus.flt.ga.GeodesicFIRnFilter;
import ch.alpine.sophus.flt.ga.GeodesicIIRnFilter;
import ch.alpine.sophus.lie.se2.Se2BiinvariantMeans;
import ch.alpine.sophus.lie.se2.Se2Group;
import ch.alpine.tensor.RationalScalar;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorUnaryOperator;

public class GeodesicCausalFilterDemo extends AbstractDatasetKernelDemo {
  protected final SpinnerLabel<GeodesicCausalFilters> spinnerCausalFilter = SpinnerLabel.of(GeodesicCausalFilters.class);
  /** parameter to blend extrapolation with measurement */
  private final JSlider jSlider = new JSlider(1, 999, 500);

  public GeodesicCausalFilterDemo() {
    super(ManifoldDisplays.SE2_ONLY, GokartPoseDataV2.INSTANCE);
    {
      spinnerCausalFilter.setValue(GeodesicCausalFilters.BIINVARIANT_MEAN_IIR);
      spinnerCausalFilter.addToComponentReduced(timerFrame.jToolBar, new Dimension(180, 28), "smoothing kernel");
      spinnerCausalFilter.addSpinnerListener(value -> updateState());
    }
    jSlider.setPreferredSize(new Dimension(500, 28));
    // ---
    timerFrame.jToolBar.add(jSlider);
    // ---
    updateState();
  }

  @Override // from RenderInterface
  protected Tensor protected_render(GeometricLayer geometricLayer, Graphics2D graphics) {
    final int radius = spinnerRadius.getValue();
    if (0 < radius) {
      ScalarUnaryOperator windowFunctions = spinnerKernel.getValue().get();
      Se2BiinvariantMeans se2BiinvariantMean = Se2BiinvariantMeans.FILTER;
      GeodesicSpace geodesicSpace = Se2Group.INSTANCE;
      TensorUnaryOperator geodesicExtrapolation = GeodesicExtrapolation.of(geodesicSpace, windowFunctions);
      // ---
      GeodesicCausalFilters geodesicCausalFilters = spinnerCausalFilter.getValue();
      // System.out.println(geodesicCausalFilters);
      // TODO ASCONA ALG should be able to do with geodesicCausalFilters.supply, but doesn't
      TensorUnaryOperator tensorUnaryOperator = geodesicCausalFilters.supply(manifoldDisplay(), windowFunctions, radius, alpha());
      tensorUnaryOperator = switch (geodesicCausalFilters) {
      case GEODESIC_FIR -> GeodesicFIRnFilter.of(geodesicExtrapolation, geodesicSpace, radius, alpha());
      case GEODESIC_IIR -> GeodesicIIRnFilter.of(geodesicExtrapolation, geodesicSpace, radius, alpha());
      case BIINVARIANT_MEAN_FIR -> BiinvariantMeanFIRnFilter.of( //
          se2BiinvariantMean, WindowSideExtrapolation.of(windowFunctions), Se2Group.INSTANCE, radius, alpha());
      case BIINVARIANT_MEAN_IIR -> BiinvariantMeanIIRnFilter.of( //
          se2BiinvariantMean, WindowSideExtrapolation.of(windowFunctions), Se2Group.INSTANCE, radius, alpha());
      };
      return tensorUnaryOperator.apply(control());
    }
    return control();
  }

  private Scalar alpha() {
    return RationalScalar.of(jSlider.getValue(), 1000);
  }

  @Override
  protected String plotLabel() {
    return super.plotLabel() + " " + alpha();
  }

  public static void main(String[] args) {
    new GeodesicCausalFilterDemo().setVisible(1000, 800);
  }
}