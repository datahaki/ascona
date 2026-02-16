// code by ob
package ch.alpine.ascona.flt;

import java.awt.Dimension;
import java.awt.Graphics2D;

import javax.swing.JSlider;

import ch.alpine.ascony.api.GeodesicCausalFilters;
import ch.alpine.ascony.dis.ManifoldDisplays;
import ch.alpine.bridge.gfx.GeometricLayer;
import ch.alpine.bridge.swing.SpinnerLabel;
import ch.alpine.sophis.flt.WindowSideExtrapolation;
import ch.alpine.sophis.flt.bm.BiinvariantMeanFIRnFilter;
import ch.alpine.sophis.flt.bm.BiinvariantMeanIIRnFilter;
import ch.alpine.sophis.flt.ga.GeodesicExtrapolation;
import ch.alpine.sophis.flt.ga.GeodesicFIRnFilter;
import ch.alpine.sophis.flt.ga.GeodesicIIRnFilter;
import ch.alpine.sophus.lie.se2.Se2BiinvariantMeans;
import ch.alpine.sophus.lie.se2.Se2Group;
import ch.alpine.sophus.math.api.GeodesicSpace;
import ch.alpine.tensor.Rational;
import ch.alpine.tensor.Scalar;
import ch.alpine.tensor.Tensor;
import ch.alpine.tensor.api.ScalarUnaryOperator;
import ch.alpine.tensor.api.TensorUnaryOperator;

// TODO ASCONA nope: 2x radius
public class GeodesicCausalFilterDemo extends AbstractSpectrogramDemo {
  protected final SpinnerLabel<GeodesicCausalFilters> spinnerCausalFilter = SpinnerLabel.of(GeodesicCausalFilters.class);
  /** parameter to blend extrapolation with measurement */
  private final JSlider jSlider = new JSlider(1, 999, 500);

  public GeodesicCausalFilterDemo() {
    super(ManifoldDisplays.SE2_ONLY);
    {
      spinnerCausalFilter.setValue(GeodesicCausalFilters.BIINVARIANT_MEAN_IIR);
      spinnerCausalFilter.addToComponent(timerFrame.jToolBar, "smoothing kernel");
      spinnerCausalFilter.addSpinnerListener(_ -> updateState());
    }
    jSlider.setPreferredSize(new Dimension(500, 28));
    // ---
    timerFrame.jToolBar.add(jSlider);
    // ---
    updateState();
  }

  @Override // from RenderInterface
  protected Tensor protected_render(GeometricLayer geometricLayer, Graphics2D graphics) {
    final int radius = param.radius;
    if (0 < radius) {
      ScalarUnaryOperator windowFunctions = gokartPoseSpec.kernel.get();
      Se2BiinvariantMeans se2BiinvariantMean = Se2BiinvariantMeans.FILTER;
      GeodesicSpace geodesicSpace = Se2Group.INSTANCE;
      TensorUnaryOperator geodesicExtrapolation = GeodesicExtrapolation.of(geodesicSpace, windowFunctions);
      // ---
      GeodesicCausalFilters geodesicCausalFilters = spinnerCausalFilter.getValue();
      // System.out.println(geodesicCausalFilters);
      // TODO ASCONA ALG should be able to do with geodesicCausalFilters.supply, but doesn't
      TensorUnaryOperator tensorUnaryOperator = geodesicCausalFilters.supply(gokartPoseSpec.manifoldDisplays.manifoldDisplay(), windowFunctions, radius,
          alpha());
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
    return Rational.of(jSlider.getValue(), 1000);
  }

  @Override
  protected String plotLabel() {
    return super.plotLabel() + " " + alpha();
  }

  static void main() {
    launch();
  }
}
